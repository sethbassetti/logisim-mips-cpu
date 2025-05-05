package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;

class PCData implements InstanceData, Cloneable {

    // The last clock value observed
    private Value lastClock;

    // The current value emitted by the PC
    private Value value;

    public static PCData get(InstanceState state, BitWidth width) {

        // First, query the "state"
        PCData ret = (PCData) state.getData();
        if (ret == null){
            ret = new PCData(null, Value.createKnown(width, 0));
            state.setData(ret);
        } else if (!ret.value.getBitWidth().equals(width)){
            ret.value = ret.value.extendWidth(width.getWidth(), Value.FALSE);
        }
        return ret;
    }

    public PCData(Value lastClock, Value value) {
        this.lastClock = lastClock;
        this.value = value;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public boolean updateClock(Value value) {
        Value old = lastClock;
        lastClock = value;
        return old == Value.FALSE && value == Value.TRUE;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value var1) {
        this.value = var1;
    }
}