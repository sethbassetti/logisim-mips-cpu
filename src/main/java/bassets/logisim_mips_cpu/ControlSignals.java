package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;

public class ControlSignals {
    public Value ALUOp;
    public boolean bne;
    public boolean beq;
    public boolean j;
    public boolean jal;
    public boolean jr;
    public boolean MemtoReg;
    public boolean MemWrite;
    public boolean RegWrite;
    public boolean ALUSrc;
    public boolean RegDst;
    public boolean display;

    public ControlSignals(String instruction) {
        // Initialize all boolean values to false
        bne = false;
        beq = false;
        j = false;
        jal = false;
        jr = false;
        MemtoReg = false;
        MemWrite = false;
        RegWrite = false;
        ALUSrc = false;
        RegDst = false;
        display = false;

        // Set default ALUOp
        ALUOp = Value.createKnown(BitWidth.THREE, 0);

        // Extract opcode and funct
        String opcode = instruction.substring(0, 4);
        int funct = Integer.parseInt(instruction.substring(13), 2);

        // Set ALUOp based on opcode and funct
        if (opcode.equals("0000")) {
            ALUOp = Value.createKnown(BitWidth.THREE, funct);
        } else if (opcode.equals("0011") || opcode.equals("0110")) {
            ALUOp = Value.createKnown(BitWidth.THREE, 6);
        } else {
            ALUOp = Value.createKnown(BitWidth.THREE, 2);
        }

        // Set other control signals based on opcode
        switch (opcode) {
            case "0000":
                RegWrite = true;
                RegDst = true;
                break;
            case "0001":
                RegWrite = true;
                ALUSrc = true;
                MemtoReg = true;
                break;
            case "0010":
                MemWrite = true;
                ALUSrc = true;
                break;
            case "0011":
                beq = true;
                break;
            case "0100":
                j = true;
                break;
            case "0101":
                RegWrite = true;
                ALUSrc = true;
                break;
            case "0110":
                bne = true;
                break;
            case "0111":
                jr = true;
                break;
            case "1000":
                jal = true;
                RegWrite = true;
                break;
            case "1111":
                display = true;
                break;
        }
    }
}