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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

class Screen extends InstanceFactory {
    private static final int SCR_WIDTH = 540;
    private static final int SCR_HEIGHT = 280;
    private static final int ULX = -540;
    private static final int ULY = -140;
    private static final int LEFT_SEP = 10;
    private static final int IN = 0;
    private static final int IN_ADDR = 1;
    private static final int IN_LOAD = 2;
    private static final int IN_DISPLAY = 5;
    private static final int OUT = 3;
    private static final int CK = 4;
    private static final int NUM_PORTS = 5;

    public Screen() {
        super("Screen");
        this.setOffsetBounds(Bounds.create(-540, -140, 540, 280));
        this.setAttributes(new Attribute[]{}, new Object[]{});
        Port[] var1 = new Port[]{
                new Port(-540, 0, "input", 16),
                new Port(-540, 30, "input", 13),
                new Port(-540, 60, "input", 1),
                new Port(0, 0, "output", 16),
                new Port(-540, -130, "input", 1),
                new Port(-540, 90, "input", 1)};
        this.setPorts(var1);
    }

    public void propagate(InstanceState state) {
        ScreenData screenData = ScreenData.get(state);
        boolean trigger = screenData.updateClock(state.getPortValue(CK));
        Value address = state.getPortValue(IN_ADDR);

        Value out;
        if (!address.isFullyDefined()) {
            out = Value.createKnown(BitWidth.create(16), 0);
            state.setPort(OUT, out, 0);
        } else {
            // Get the value from screen data and set it to the output port
            out = screenData.getValue(address);
            state.setPort(OUT, out, 0);
        }
        if (trigger) {
            // Then, if we are loading, load to the screen data
            if (state.getPortValue(IN_LOAD) == Value.TRUE) {
                Value in = state.getPortValue(IN);
                if (in.isFullyDefined()) {
                    screenData.setValue(in, address);
                }
            }

        }


        if (state.getPortValue(IN_DISPLAY) == Value.TRUE) {
            screenData.display();
        }
    }

    public void paintInstance(InstancePainter var1) {
        Bounds var2 = var1.getBounds();
        var1.drawRectangle(var1.getBounds(), "");
        Graphics var3 = var1.getGraphics();
        var3.setColor(Color.BLACK);
        var1.drawClockSymbol(var2.getX(), var2.getY() + 10);
        var1.drawPort(4, "", Direction.EAST);
        var1.drawPort(0, "I", Direction.EAST);
        var1.drawPort(3, "O", Direction.WEST);
        var1.drawPort(1, "A", Direction.EAST);
        var1.drawPort(2, "L", Direction.EAST);
        var1.drawPort(5, "Display", Direction.EAST);

        ScreenData var5 = ScreenData.get(var1);
        int var6 = (540 - var5.image.getWidth() * 3) / 2;
        int var7 = (280 - var5.image.getHeight() * 3) / 2;
        var3.drawImage(var5.image, var2.getX() + var6, var2.getY() + var7, var5.image.getWidth() * 3, var5.image.getHeight() * 3, (ImageObserver)null);
    }
}