package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.GraphicsUtil;

public class Control extends InstanceFactory {
    private Port[] ports;
    private int HEIGHT = 250;
    private int WIDTH = 100;

    Control(){
        super("Control");

        setOffsetBounds(Bounds.create(0, 0, WIDTH, HEIGHT));
        // Control Signals
        // bne, beq, j, jal, jr, MemToReg, MemWrite, RegWrite, ALUSrc, RegDst, ALUOp
        // 11 control signals
        ports = new Port[]{
                new Port(0, 30, Port.INPUT, BitWidth.FOUR), // 0 = OPCODE
                new Port(0, 60, Port.INPUT, BitWidth.THREE), // 1 = Funct
                new Port(WIDTH, 20, Port.OUTPUT, BitWidth.ONE), // 2 = bne
                new Port(WIDTH, 40, Port.OUTPUT, BitWidth.ONE), // 3 = beq
                new Port(WIDTH, 60, Port.OUTPUT, BitWidth.ONE), //4 = j
                new Port(WIDTH, 80, Port.OUTPUT, BitWidth.ONE), // 5 = jal
                new Port(WIDTH, 100, Port.OUTPUT, BitWidth.ONE), // 6 = jr
                new Port(WIDTH, 120, Port.OUTPUT, BitWidth.ONE), // 7 = MemtoReg
                new Port(WIDTH, 140, Port.OUTPUT, BitWidth.ONE), // 8 = MemWrite
                new Port(WIDTH, 160, Port.OUTPUT, BitWidth.ONE), // 9 = RegWrite
                new Port(WIDTH, 180, Port.OUTPUT, BitWidth.ONE), // 10 = ALUSrc
                new Port(WIDTH, 200, Port.OUTPUT, BitWidth.ONE), // 11 = RegDst
                new Port(WIDTH, 220, Port.OUTPUT, BitWidth.THREE), // 12 = ALUOp

        };

        this.setPorts(ports);
    }

    @Override
    public void paintInstance(InstancePainter instancePainter) {

        // Add "Center" text near the top of the component
        GraphicsUtil.drawCenteredText(instancePainter.getGraphics(), "Control", instancePainter.getBounds().x + WIDTH / 2, instancePainter.getBounds().y + 10);

        instancePainter.drawBounds();
        instancePainter.drawPort(0, "OpCode", Direction.EAST);
        instancePainter.drawPort(1, "Funct", Direction.EAST);
        instancePainter.drawPort(2, "bne", Direction.WEST);
        instancePainter.drawPort(3, "beq", Direction.WEST);
        instancePainter.drawPort(4, "j", Direction.WEST);
        instancePainter.drawPort(5, "jal", Direction.WEST);
        instancePainter.drawPort(6, "jr", Direction.WEST);
        instancePainter.drawPort(7, "MemtoReg", Direction.WEST);
        instancePainter.drawPort(8, "MemWrite", Direction.WEST);
        instancePainter.drawPort(9, "RegWrite", Direction.WEST);
        instancePainter.drawPort(10, "ALUSrc", Direction.WEST);
        instancePainter.drawPort(11, "RegDst", Direction.WEST);
        instancePainter.drawPort(12, "ALUOp", Direction.WEST);
    }

    @Override
    public void propagate(InstanceState instanceState) {
        String opcode = instanceState.getPortValue(0).toBinaryString();
        int funct = instanceState.getPortValue(1).toIntValue();

        Value ALUOp;

        // First, figure out the ALUOp field
        if (opcode.equals("0000")){
            ALUOp = Value.createKnown(BitWidth.THREE, funct);
        } else if(opcode.equals("0011") || opcode.equals("0110")){
            ALUOp = Value.createKnown(BitWidth.THREE, 6);
        } else {
            ALUOp = Value.createKnown(BitWidth.THREE, 2);
        }

        boolean bne = false;
        boolean beq = false;
        boolean j = false;
        boolean jal = false;
        boolean jr = false;
        boolean MemtoReg = false;
        boolean MemWrite = false;
        boolean RegWrite = false;
        boolean ALUSrc = false;
        boolean RegDst = false;


        // Now, figure out all other control bits
        if (opcode.equals("0000")){
            RegWrite = true;
            RegDst = true;
        } else if (opcode.equals("0001")){
            RegWrite = true;
            ALUSrc = true;
            MemtoReg = true;
        } else if (opcode.equals("0010")){
            MemWrite = true;
            ALUSrc = true;
        } else if (opcode.equals("0011")){
            beq = true;
        } else if (opcode.equals("0100")){
            j = true;
        } else if (opcode.equals("0101")){
            RegWrite = true;
            ALUSrc = true;
        } else if (opcode.equals("0110")){
            bne = true;
        } else if(opcode.equals("0111")){
            jr = true;
        } else if (opcode.equals("1000")){
            jal = true;
            RegWrite = true;
        }

        instanceState.setPort(2, Value.createKnown(BitWidth.ONE, bne ? 1 : 0), 0);
        instanceState.setPort(3, Value.createKnown(BitWidth.ONE, beq ? 1 : 0), 0);
        instanceState.setPort(4, Value.createKnown(BitWidth.ONE, j ? 1 : 0), 0);
        instanceState.setPort(5, Value.createKnown(BitWidth.ONE, jal ? 1 : 0), 0);
        instanceState.setPort(6, Value.createKnown(BitWidth.ONE, jr ? 1 : 0), 0);
        instanceState.setPort(7, Value.createKnown(BitWidth.ONE, MemtoReg ? 1 : 0), 0);
        instanceState.setPort(8, Value.createKnown(BitWidth.ONE, MemWrite ? 1 : 0), 0);
        instanceState.setPort(9, Value.createKnown(BitWidth.ONE, RegWrite ? 1 : 0), 0);
        instanceState.setPort(10, Value.createKnown(BitWidth.ONE, ALUSrc ? 1 : 0), 0);
        instanceState.setPort(11, Value.createKnown(BitWidth.ONE, RegDst ? 1 : 0), 0);
        instanceState.setPort(12, ALUOp, 0);

    }
}
