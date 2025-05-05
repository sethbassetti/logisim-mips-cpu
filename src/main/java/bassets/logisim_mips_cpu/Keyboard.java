package bassets.logisim_mips_cpu;


import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.*;

import java.awt.event.KeyEvent;

class Keyboard extends InstanceFactory {
    public static class Poker extends InstancePoker{
        @Override
        public void keyPressed(InstanceState state, KeyEvent e) {
            KeyboardData data = (KeyboardData) state.getData();
            if(e.getKeyCode() == data.value.toIntValue()){
                e.consume();
            } else {
                data.value = Value.createKnown(BitWidth.create(16), e.getKeyCode());

                e.consume();
                state.getInstance().fireInvalidated();

            }

        }

        @Override
        public void keyReleased(InstanceState state, KeyEvent e) {
            KeyboardData data = (KeyboardData) state.getData();

            data.value = Value.createKnown(BitWidth.create(16), 0);
            e.consume();
            state.getInstance().fireInvalidated();

        }
    }

    public Keyboard() {
        super("Keyboard");
        this.setAttributes(
                new Attribute[]{}, new Object[]{}
        );

        setInstancePoker(Poker.class);

        setOffsetBounds(Bounds.create(0, 0, 160, 20));
        Port[] ports = new Port[]{
                new Port(160,10, Port.OUTPUT, 16), // 5 - output
        };

        setPorts(ports);
    }

    public void propagate(InstanceState state) {
        // Get the
        KeyboardData keyboardData = KeyboardData.get(state);

        // Get the value from the port
        state.setPort(0, keyboardData.getData(), 1);
    }

    public void paintInstance(InstancePainter painter) {
        Bounds bounds = painter.getBounds();
        
        // Draw the main rectangle
        painter.drawRectangle(bounds, "");
        
        // Draw red outline if keyboard is active
        if (painter.getShowState()) {
            InstanceData data = painter.getData();
            if (data instanceof KeyboardData) {
                KeyboardData keyboardData = (KeyboardData) data;
                boolean isActive = keyboardData.getData().toIntValue() != 0;
                
                if (isActive) {
                    painter.getGraphics().setColor(java.awt.Color.RED);
                    painter.getGraphics().drawRect(bounds.getX(), bounds.getY(), 
                                                 bounds.getWidth() - 1, bounds.getHeight() - 1);
                    painter.getGraphics().setColor(java.awt.Color.BLACK); // Reset color
                }
            }
        }

        painter.drawPorts();
    }

}