package bassets.logisim_mips_cpu;

import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;

public class KeyboardData implements InstanceData, Cloneable{

    private Value lastClock;
    public Value value;

    public static KeyboardData get(InstanceState state) {
        KeyboardData cpuData = (KeyboardData) state.getData();

        if (cpuData == null){
            cpuData = new KeyboardData(null);
            state.setData(cpuData);
        }
        return cpuData;

    }
    public KeyboardData(Value lastClock) {
        this.lastClock = lastClock;
        this.value = Value.createKnown(BitWidth.create(16), 0);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public Value getData(){
        return value;
    }


}
