package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class PCPoker extends InstancePoker {
    private int initValue;
    private int curValue;
    public PCPoker(){

    }

    @Override
    public boolean init(InstanceState state, MouseEvent e) {
        PCData data = (PCData) state.getData();

        initValue = data.getValue().toIntValue();
        curValue = initValue;
        return true;
    }

    @Override
    public void keyTyped(InstanceState state, KeyEvent e) {
        int val = Character.digit(e.getKeyChar(), 16);
        if (val < 0)
            return;
        e.consume();
        BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
        if (dataWidth == null)
            dataWidth = BitWidth.create(8);
        curValue = (curValue * 16 + val) & dataWidth.getMask();
        PCData data = (PCData) state.getData();
        data.setValue(Value.createKnown(dataWidth, curValue));

        state.fireInvalidated();
    }

    @Override
    public void keyPressed(InstanceState state, KeyEvent e) {
        BitWidth dataWidth = state.getAttributeValue(StdAttr.WIDTH);
        if (dataWidth == null)
            dataWidth = BitWidth.create(8);
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            int maxVal = dataWidth.getMask();
            if (curValue != maxVal) {
                curValue = curValue + 1;
                PCData data = (PCData) state.getData();
                data.setValue(Value.createKnown(dataWidth, curValue));
                state.fireInvalidated();
            }
            e.consume();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (curValue != 0) {
                curValue = curValue - 1;
                PCData data = (PCData) state.getData();
                data.setValue(Value.createKnown(dataWidth, curValue));
                state.fireInvalidated();
            }
            e.consume();
        }
    }
    public void paint(InstancePainter painter){
        Bounds bds = painter.getBounds();
        BitWidth width = painter.getAttributeValue(StdAttr.WIDTH);
        int len = (width.getWidth() + 3) / 4;

        Graphics g = painter.getGraphics();
        g.setColor(Color.RED);

        int wid = 20 * len + 2; // width of caret rectangle
        int ht = 13; // height of caret rectangle
        g.drawRect(bds.getX() + (bds.getWidth() - wid) / 2,
                bds.getY() + (bds.getHeight() - ht) / 2 + 3, wid, ht);
        g.setColor(Color.BLACK);

    }

}
