package bassets.logisim_mips_cpu;

import static com.cburch.logisim.std.Strings.S;

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
import com.cburch.logisim.util.StringUtil;
import java.awt.Color;
import java.awt.Graphics;

class PC extends InstanceFactory {
    private static final BitWidth BIT_WIDTH = BitWidth.create(8);

    public PC() {
        super("PC");
        this.setAttributes(
                new Attribute[]{StdAttr.WIDTH}, new Object[]{BIT_WIDTH}
        );

        setInstancePoker(PCPoker.class);

        setOffsetBounds(Bounds.create(-160, -20, 160, 40));
        Port[] ports = new Port[]{
                new Port(-80, 20, Port.INPUT, 1), // 0 - Clock
                new Port(-160, 0, Port.INPUT, StdAttr.WIDTH), // 1 - In
                new Port(-120, -20, Port.INPUT, 1),       // 2 - Reset
                new Port(-80, -20, Port.INPUT, 1), // 3 - Load
                new Port(-40, -20, Port.INPUT, 1), // 4 - Increment
                new Port(0,0, Port.OUTPUT, StdAttr.WIDTH), // 5 - output
        };
        ports[0].setToolTip(S.getter("Clock: memory value updates on clock trigger"));
        ports[1].setToolTip(S.getter("Input: What to load the PC with"));
        ports[2].setToolTip(S.getter("Reset: If 1, then resets PC"));
        ports[3].setToolTip(S.getter("Load: If 1, then loads input into PC"));
        ports[4].setToolTip(S.getter("Increment: If 1, then PC increments on clock cycles."));

        setPorts(ports);
    }

    public void propagate(InstanceState state) {
        BitWidth width = state.getAttributeValue(StdAttr.WIDTH);
        PCData cur = PCData.get(state, width);

        boolean trigger = cur.updateClock(state.getPortValue(0));

        int nextValue;

        // Port 2 - Reset
        if (state.getPortValue(2).toIntValue() == 1){
            nextValue = 0;
        }
        else if(state.getPortValue(4).toIntValue() == 1){
            nextValue = cur.getValue().toIntValue() + 1;
        }
        // Else, stay the same
        else {
            nextValue = cur.getValue().toIntValue();
        }

        // If triggered (clock update), here is how we handle it
        if (trigger && state.getPortValue(3).toIntValue() == 1 && state.getPortValue(3).isFullyDefined()){
            nextValue = state.getPortValue(1).toIntValue();
        }

        cur.setValue(Value.createKnown(BIT_WIDTH, nextValue));

        // Remember to update the output port
        state.setPort(5, cur.getValue(), 1);

    }

    public void paintInstance(InstancePainter painter) {
        Bounds bounds = painter.getBounds();
        painter.drawRectangle(bounds, "");

        BitWidth width = painter.getAttributeValue(StdAttr.WIDTH);
        PCData data = PCData.get(painter, width);

        GraphicsUtil.drawCenteredText(painter.getGraphics(), "PC:" + StringUtil.toHexString(width.getWidth(), data.getValue().toIntValue()), bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);

        // Draw the clock symbol
        int leftSide = bounds.getX();
        int midX = leftSide + bounds.getWidth() / 2;
        int bottomSide = bounds.getY() + bounds.getHeight();
        int midY = bottomSide - bounds.getHeight() / 2;
        int triangleSize = 7;

        // Draw Clock Symbol
        painter.getGraphics().drawPolygon(
                new int[]{midX - triangleSize, midX, midX + triangleSize},
                new int[]{bottomSide, bottomSide - triangleSize, bottomSide},
                3
                );
        painter.drawPort(0);

        // Draw flags
        painter.drawPort(1, "in", Direction.EAST);
        painter.drawPort(2, "reset", Direction.NORTH);
        painter.drawPort(3, "load", Direction.NORTH);
        painter.drawPort(4, "inc", Direction.NORTH);
        painter.drawPort(5, "out", Direction.WEST);

    }

}