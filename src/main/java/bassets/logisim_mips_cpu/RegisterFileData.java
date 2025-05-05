package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;

public class RegisterFileData implements InstanceData, Cloneable{
    public static RegisterFileData get(InstanceState state){
        RegisterFileData rfData = (RegisterFileData) state.getData();

        if (rfData == null){
            // If it doesn't yet exist, then we'll set it up with our default
            // values and put it into the circuit state so it can be retrieved
            // in future propagations.
            rfData = new RegisterFileData(null, new short[8]);
            state.setData(rfData);
        }

        return rfData;
    }

    // The last clock value observed
    private Value lastClock;

    // Register file stores 8 registers of 16 bits each
    short[] registers;

    public RegisterFileData(Value lastClock, short[] registerValues){
        this.lastClock = lastClock;
        this.registers = registerValues;
    }

    public boolean updateClock(Value value){
        Value old = lastClock;
        lastClock = value;
        return old == Value.FALSE && value == Value.TRUE;
    }

    public Value read(int address){
        return Value.createKnown(BitWidth.create(16), registers[address]);
    }

    public void reset(){
        registers = new short[8];
    }

    public short[] getRegisters(){
        return registers;
    }

    public void write(int address, Value writeData){
        if (address == 0) return;
        short data;

        data = (short) writeData.toIntValue();
        registers[address] = data;
    }

    @Override
    public Object clone() {
        try {
            RegisterFileData cloned = (RegisterFileData) super.clone();

            cloned.registers = new short[8];
            return cloned;
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

}
