package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;

class CPUData implements InstanceData, Cloneable {
    private Value lastClock;
    public Value regPC;
    public short[] registers;

    public static CPUData get(InstanceState state) {
        CPUData cpuData = (CPUData) state.getData();

        if (cpuData == null){
            cpuData = new CPUData(null);
            state.setData(cpuData);
        }
        return cpuData;

    }

    public CPUData(Value clock) {
        this.lastClock = clock;
        this.regPC = Value.createKnown(BitWidth.create(16), 0);
        this.registers = new short[8];
        this.registers[6] = 16383;
    }

    public boolean updateClock(Value value){
        Value old = lastClock;
        lastClock = value;
        return old == Value.FALSE && value == Value.TRUE;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

}