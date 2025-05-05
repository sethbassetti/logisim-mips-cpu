package bassets.logisim_mips_cpu;


import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics;

class Inc extends InstanceFactory {
    Inc() {
        super("Inc16");
        setAttributes(
                new Attribute[]{StdAttr.WIDTH},
                new Object[]{BitWidth.create(16)}
        );
        setOffsetBounds(Bounds.create(0, 0, 40, 40));
        Port[] ps = new Port[]{
                new Port(0, 20, Port.INPUT, StdAttr.WIDTH),
                new Port(40, 20, Port.OUTPUT, StdAttr.WIDTH)
        };
        setPorts(ps);
    }

    public void propagate(InstanceState state) {
        Value in = state.getPortValue(0);
        int nextVal = in.toIntValue() + 1;

        Value out = Value.createKnown(in.getBitWidth(), nextVal);
        state.setPort(1, out, 0);

    }

    public void paintInstance(InstancePainter var1) {
        Bounds var2 = var1.getBounds();
        var1.drawRectangle(var1.getBounds(), "");
        Graphics var3 = var1.getGraphics();
        var3.setColor(Color.BLACK);
        GraphicsUtil.drawCenteredText(var3, "Inc16", var2.getX() + var2.getWidth() / 2, var2.getY() + var2.getHeight() / 2);
        var1.drawPort(0);
        var1.drawPort(1);
    }
}