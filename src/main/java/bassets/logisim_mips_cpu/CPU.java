package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics;

import static com.cburch.logisim.std.Strings.S;

class CPU extends InstanceFactory {
    private static final int SCR_WIDTH = 290;
    private static final int SCR_HEIGHT = 180;
    private static final int ULX = -290;
    private static final int ULY = -90;
    private static final int LEFT_SEP = 10;
    private static final int P_INSTR = 0;
    private static final int P_IN = 1;
    private static final int P_RESET = 2;
    private static final int P_OUT = 3;
    private static final int P_ADDR = 4;
    private static final int P_WRITE = 5;
    private static final int P_DISPLAY = 8;
    private static final int P_PC = 6;
    private static final int CK = 7;
    private static final int NUM_PORTS = 8;

    public CPU() {
        super("CPU");
        setOffsetBounds(Bounds.create(0, 0, 290, 180));
        setAttributes(new Attribute[]{}, new Object[]{});
        Port[] ports = new Port[]{
                new Port(0, 60, "input", 16),       // 0 = Instruction
                new Port(0, 90, "input", 16),  // 1 = MemData
                new Port(0, 120, "input", 1),         // 2 = Reset
                new Port(290, 30, "output", 16),  // 3 = MemWriteData
                new Port(290, 110, "output", 15),          // 4 = MemAddress
                new Port(290, 70, "output", 1),      // 5 = MemWrite
                new Port(290, 150, "output", 15),        // 6 = PC
                new Port(0, 10, "input", 1),      // 7 = Clock
                new Port(290, 170, "output", 1)        // 8 = Display;
        };

        ports[0].setToolTip(S.getter("16-bit instruction from ROM"));
        ports[1].setToolTip(S.getter("16-bit incoming data from RAM"));
        ports[2].setToolTip(S.getter("Reset: If 1, then resets PC and Register File"));
        ports[3].setToolTip(S.getter("16-bit data to write to RAM"));
        ports[4].setToolTip(S.getter("15-bit address to write to RAM"));
        ports[5].setToolTip(S.getter("MemWrite: If 1, then write data to RAM"));
        ports[6].setToolTip(S.getter("15-bit address to read from ROM"));
        ports[7].setToolTip(S.getter("Clock: memory value updates on clock trigger"));

        setPorts(ports);
    }

    public static int signedBinaryToInt(String binary, int bitWidth) {
        int value = Integer.parseInt(binary, 2);
        // Sign extend if negative (MSB is 1)
        if ((value & (1 << (bitWidth - 1))) != 0) {
            value |= (-1 << bitWidth);
        }
        return value;
    }

    public void propagate(InstanceState state) {
        // Quick exit check for undefined values - combine all checks into one
        Value instrValue = state.getPortValue(P_INSTR);
        Value inValue = state.getPortValue(P_IN);
        Value resetValue = state.getPortValue(P_RESET);

        if (!instrValue.isFullyDefined() || !inValue.isFullyDefined() || !resetValue.isFullyDefined()) {
            // Create values once and reuse
            Value zeroFifteen = Value.createKnown(BitWidth.create(15), 0);
            Value zeroSixteen = Value.createKnown(BitWidth.create(16), 0);
            Value zeroBit = Value.createKnown(BitWidth.ONE, 0);

            state.setPort(P_PC, zeroFifteen, 0);
            state.setPort(P_OUT, zeroSixteen, 0);
            state.setPort(P_ADDR, zeroFifteen, 0);
            state.setPort(P_WRITE, zeroBit, 0);
            return;
        }

        CPUData cpuData = CPUData.get(state);

        // Handle reset immediately
        if (resetValue.toIntValue() == 1) {
            cpuData.registers = new short[8];
            cpuData.registers[6] = 16383;
            cpuData.regPC = Value.createKnown(BitWidth.create(16), 0);
        }

        // Early exit if not triggered
        boolean trigger = cpuData.updateClock(state.getPortValue(CK));

        // Cache instruction binary string to avoid repeated conversion
        String instr = instrValue.toBinaryString();

        // Pre-calculate all instruction fields at once
        int rs = Integer.parseInt(instr.substring(4, 7), 2);
        int rt = Integer.parseInt(instr.substring(7, 10), 2);
        int rd = Integer.parseInt(instr.substring(10, 13), 2);
        int imm = signedBinaryToInt(instr.substring(10), 6);
        int jumpImm = Integer.parseInt(instr.substring(4), 2);

        // Parse control signals once
        ControlSignals controls = new ControlSignals(instr);

        // Cache register values
        short[] registers = cpuData.registers;
        short readData1 = registers[rs];
        short readData2 = registers[rt];

        // Determine ALU input B once
        short b = controls.ALUSrc ? (short)imm : readData2;

        // Optimize ALU operation using lookup table instead of switch
        short result = executeALU(controls.ALUOp.toBinaryString(), readData1, b);

        // Calculate result == 0 only once
        boolean isZero = (result == 0);

        // Handle register write
        if (controls.RegWrite && trigger) {
            short writeData;
            if (controls.MemtoReg) {
                writeData = (short)inValue.toIntValue();
            } else if (controls.jal) {
                writeData = (short)(cpuData.regPC.toIntValue() + 1);
            } else {
                writeData = result;
            }

            // Optimize register write target selection
            if (controls.RegDst) {
                registers[rd] = writeData;
            } else if (controls.jal) {
                registers[7] = writeData;
            } else {
                registers[rt] = writeData;
            }
        }

        // Create memory values only once
        Value memWriteData = Value.createKnown(BitWidth.create(16), readData2);
        Value memAddress = Value.createKnown(BitWidth.create(15), result);
        Value memWrite = Value.createKnown(BitWidth.ONE, controls.MemWrite ? 1 : 0);

        // Set all ports at once
        state.setPort(P_OUT, memWriteData, 0);
        state.setPort(P_ADDR, memAddress, 0);
        state.setPort(P_WRITE, memWrite, 0);

        // Optimize PC calculation
        short pc;
        if (controls.jr) {
            pc = readData1;
        } else if (controls.j || controls.jal) {
            pc = (short)jumpImm;
        } else if ((controls.beq && isZero) || (controls.bne && !isZero)) {
            pc = (short)(cpuData.regPC.toIntValue() + 1 + imm);
        } else {
            pc = (short)(cpuData.regPC.toIntValue() + 1);
        }

        if (trigger) {
            cpuData.regPC = Value.createKnown(BitWidth.create(16), pc);
        }

        state.setPort(P_PC, cpuData.regPC, 0);
        state.setPort(P_DISPLAY, Value.createKnown(BitWidth.ONE, controls.display ? 1 : 0), 0);
    }

    // Separate ALU method using lookup table for operations
    private static final short executeALU(String op, short a, short b) {
        switch (op) {
            case "000": return (short)(a & b);
            case "001": return (short)(a | b);
            case "010": return (short)(a + b);
            case "100": return (short)(a & ~b);
            case "101": return (short)(a | ~b);
            case "110": return (short)(a - b);
            case "111": return (short)(a < b ? 1 : 0);
            default: return 0;
        }
    }

    public void paintInstance(InstancePainter painter) {
        Bounds bounds = painter.getBounds();
        painter.drawBounds();

        painter.drawClock(7, Direction.EAST);
        painter.drawPort(1, "memData", Direction.EAST);
        painter.drawPort(0, "instr", Direction.EAST);
        painter.drawPort(2, "reset", Direction.EAST);
        painter.drawPort(3, "memWriteData", Direction.WEST);
        painter.drawPort(5, "memWrite", Direction.WEST);
        painter.drawPort(4, "memAddress", Direction.WEST);
        painter.drawPort(6, "pc", Direction.WEST);
        painter.drawPort(8, "display", Direction.WEST);

        // Display register values if showing state
        if (painter.getShowState()) {
            CPUData rfData = CPUData.get(painter);
            short[] registers = rfData.registers;

            // Calculate layout parameters
            int cellWidth = 80;               // Width of each cell
            int cellHeight = 20;              // Height of each cell
            int startX = bounds.getX() + bounds.getWidth() / 2 - cellWidth / 2;  // Center position
            int startY = bounds.getY() + 10;  // Top margin

            // Draw each register value in a single column with table cells
            for (int i = 0; i < 8; i++) {
                int y = startY + i * cellHeight;

                // Draw the cell rectangle
                painter.getGraphics().drawRect(startX, y, cellWidth, cellHeight);

                // Convert short to signed integer string
                String value = String.valueOf((int) registers[i]);

                // Draw the register number and its value, centered in the cell
                int textX = startX + 10;  // Add padding from left edge of cell
                int textY = y + 15;       // Center text vertically in cell
                painter.getGraphics().drawString("R" + i + ": " + value, textX, textY);

            }
        }}}