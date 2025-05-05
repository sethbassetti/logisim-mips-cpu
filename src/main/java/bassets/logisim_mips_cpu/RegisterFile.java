package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.util.GraphicsUtil;

public class RegisterFile extends InstanceFactory {

    public RegisterFile(){
        super("RegisterFile");

        // Set up the component's attributes and it's dimensions
        setAttributes(new Attribute[]{}, new Object[]{});
        int width = 300;
        int height = 250;

        int heightInterval = height / 5;


        setOffsetBounds(Bounds.create(0, 0, width, height));

        Port[] ports = new Port[]{
                new Port(0, height / 5, Port.INPUT, 3), // 0 = Read Register 1
                new Port(0, height / 5 * 2, Port.INPUT, 3), // 1 = Read Register 2
                new Port(0, height / 5 * 3, Port.INPUT, 3), // 2 = Write Register
                new Port(0, height / 5 * 4, Port.INPUT, 16), // 3 = Write Data
                new Port(Math.round((width / 4 * 2) / 10.0f) * 10, height, Port.INPUT, 1), // 4 = Write Enable
                new Port(width, height / 3 / 10 * 10, Port.OUTPUT, 16), // 5 = Read Data 1
                new Port(width, height / 3 * 2 / 10 * 10, Port.OUTPUT, 16), // 6 = Read Data 2
                new Port(Math.round(width / 4 / 10.0f) * 10, height, Port.INPUT, 1) ,// 7 = Clock
                new Port(Math.round((width / 4 * 3) / 10.0f) * 10, height, Port.INPUT, 1) // 8 = Reset

        };
        setPorts(ports);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Bounds bounds = painter.getBounds();
        painter.drawRectangle(bounds, "Register File");

        painter.drawPort(0, "Read Register 1", Direction.EAST);
        painter.drawPort(1, "Read Register 2", Direction.EAST);
        painter.drawPort(2, "Write Register", Direction.EAST);
        painter.drawPort(3, "Write Data", Direction.EAST);
        painter.drawPort(4, "Write Enable", Direction.SOUTH);
        painter.drawPort(5, "Read Data 1", Direction.WEST);
        painter.drawPort(6, "Read Data 2", Direction.WEST);
        painter.drawClock(7, Direction.NORTH);
        painter.drawPort(8, "Reset", Direction.SOUTH);

        // Display register values if showing state
        if (painter.getShowState()) {
            RegisterFileData rfData = RegisterFileData.get(painter);
            short[] registers = rfData.getRegisters();

            // Calculate layout parameters
            int cellWidth = 80;               // Width of each cell
            int cellHeight = 20;              // Height of each cell
            int startX = bounds.getX() + bounds.getWidth() / 2 - cellWidth / 2;  // Center position
            int startY = bounds.getY() + 30;  // Top margin

            // Draw each register value in a single column with table cells
            for (int i = 0; i < 8; i++) {
                int y = startY + i * cellHeight;

                // Draw the cell rectangle
                painter.getGraphics().drawRect(startX, y, cellWidth, cellHeight);

                // Convert short to signed integer string
                String value = String.valueOf((int) registers[i]);

                // Draw the register number and its value, centered in the cell
                int textX = startX + 10;  // Add padding from left edge of cell
                int textY = y + 15;       // Center text vertically in cell
                painter.getGraphics().drawString("R" + i + ": " + value, textX, textY);
            }
        }


    }

    @Override
    public void propagate(InstanceState instanceState) {
        RegisterFileData rfData =  RegisterFileData.get(instanceState);

        boolean trigger = rfData.updateClock(instanceState.getPortValue(7));

        // No matter what, read from memory
        Value address1;
        Value address2;
        Value writeAddress;
        Value readData1;
        Value readData2;
        Value writeData;


        // If we are on a rising edge, then update memory
        if (trigger && (instanceState.getPortValue(4) == Value.TRUE)){
            // First, get the write address
            writeAddress = instanceState.getPortValue(2);
            // Then, get the write data
            writeData = instanceState.getPortValue(3);

            // Then, write to RfData
            if (writeAddress.isFullyDefined()) {
                rfData.write(writeAddress.toIntValue(), writeData);
            }


        }

        // Read first port first
        address1 = instanceState.getPortValue(0);
        if (!address1.isFullyDefined()){
            readData1 = Value.createUnknown(16);
        } else {
            readData1 = rfData.read(address1.toIntValue());
        }
        instanceState.setPort(5, readData1, 1);

        // Read second port second
        address2 = instanceState.getPortValue(1);
        if (!address2.isFullyDefined()){
            readData2 = Value.createUnknown(16);
        } else {
            readData2 = rfData.read(address2.toIntValue());
        }
        instanceState.setPort(6, readData2, 1);


        if (instanceState.getPortValue(8) == Value.TRUE){
            rfData.reset();
        }

    }


}
