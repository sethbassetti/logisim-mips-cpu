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
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics;

class ALU extends InstanceFactory {
    private static final int ALU_WIDTH = 200;
    private static final int ALU_HEIGHT = 80;
    private Port[] ports;
    private static final int TOP_GAP_WIDTH = (int) (0.3 * ALU_WIDTH);
    private static final int TOP_GAP_HEIGHT = TOP_GAP_WIDTH / 2;

    ALU() {
        super("ALU");

        // Allow us to change the bit-width of the ALU.
        // By Default 8
        this.setAttributes(new Attribute[]{StdAttr.WIDTH, StdAttr.FACING}, new Object[]{BitWidth.create(8), Direction.SOUTH});
        this.setOffsetBounds(Bounds.create(0, 0, ALU_WIDTH, ALU_HEIGHT));

        setFacingAttribute(StdAttr.FACING);

        // Define all the ports, their positions, type, and bitwidth
        ports = new Port[]{
                new Port(40, 0, Port.INPUT, StdAttr.WIDTH),         // 0 - A
                new Port(160, 0, Port.INPUT, StdAttr.WIDTH),        // 1 - B
                new Port(40, 40, Port.INPUT, BitWidth.THREE),       // 2 - OP
                new Port(160, 40, Port.OUTPUT, BitWidth.ONE),       // 3 - Zero
                new Port(100, 80, Port.OUTPUT, StdAttr.WIDTH)       // 4 - Out
        };

        this.setPorts(ports);


    }


    public void propagate(InstanceState state) {

        // Extract the binary string of the operation we are performing
        String opPort = state.getPortValue(2).toBinaryString();

        // The two operands of the ALU
        Value a = state.getPortValue(0);
        Value b = state.getPortValue(1);

        BitWidth aluWidth = a.getBitWidth();

        int result;

        switch (opPort){
            case "000" -> result = a.toIntValue() & b.toIntValue();
            case "001" -> result = a.toIntValue() | b.toIntValue();
            case "010" -> result = a.toIntValue() + b.toIntValue();
            case "100" -> result = a.toIntValue() & ~b.toIntValue();
            case "101" -> result = a.toIntValue() | ~b.toIntValue();
            case "110" -> result = a.toIntValue() - b.toIntValue();
            case "111" -> result = Integer.parseInt(a.toDecimalString(true)) < Integer.parseInt(b.toDecimalString(true)) ? 1 : 0;
            default -> result = 0;

        }

        // Check to see if the result == 0
        Value zero = Value.createKnown(BitWidth.ONE, result == 0 ? 1 : 0);
        Value out = Value.createKnown(aluWidth, result);

        state.setPort(3, zero, 0);
        state.setPort(4, out, 0);
    }

    public void paintInstance(InstancePainter painter) {

        Graphics graphics = painter.getGraphics();
        Bounds bounds = painter.getBounds();

        // We need to where the x points are for the divot at the top of the ALU
        // Ideally, it is centered and the height is half of the width of the divot "triangle"
        int topGapStartX = ALU_WIDTH / 2 - TOP_GAP_WIDTH / 2;
        int topGapMidX = ALU_WIDTH / 2;
        int topGapEndX = ALU_WIDTH / 2 + TOP_GAP_WIDTH / 2;


        // Define all the points for the ALU polygon
        int[] xPoints = {0, topGapStartX, topGapMidX, topGapEndX, bounds.getWidth(), ALU_WIDTH - ALU_HEIGHT, ALU_HEIGHT};
        int[] yPoints = {0, 0,  TOP_GAP_HEIGHT,  0,   0,  ALU_HEIGHT, ALU_HEIGHT};

        // Make sure the polygon points are relative to where the ALU component is by adding the x and y anchor point
        for (int i=0; i < xPoints.length; i++){
            xPoints[i] += bounds.getX();
            yPoints[i] += bounds.getY();
        }

        // Draw the ALU
        graphics.drawPolygon(xPoints, yPoints, 7);

        // The Zero and Op labels are too squished so draw these labels manually
        // There was a little bit of manual finagling to get the label positions to look nice
        GraphicsUtil.drawCenteredText(graphics, "Zero", bounds.getX() + topGapEndX + 10, bounds.getY() + ALU_HEIGHT / 2 - 2);
        GraphicsUtil.drawCenteredText(graphics, "Op", bounds.getX() + topGapStartX - 10, bounds.getY() + ALU_HEIGHT / 2 - 2);

        // Draw the ports and label them if automatic labelling is appropriate
        painter.drawPort(0, "A",Direction.NORTH);
        painter.drawPort(1, "B", Direction.NORTH);
        painter.drawPort(2);
        painter.drawPort(3);
        painter.drawPort(4, "Out", Direction.SOUTH);


        // Determine what the operation we are performing is and display that on the ALU
        Value op = painter.getPortValue(2);

        // Center the Op on the ALU
        int centerX = bounds.getX() + ALU_WIDTH / 2;
        int centerY = bounds.getY() + ALU_HEIGHT / 2;


        // This determines the appropriate text to display based on the opcode
        String opText;
        switch (op.toBinaryString()){
            case "000" -> opText = "A&B";
            case "001" -> opText = "A||B";
            case "010" -> opText = "A+B";
            case "100" -> opText = "A&!B";
            case "101" -> opText = "A||!B";
            case "110" -> opText = "A-B";
            case "111" -> opText = "A<B";
            default    -> opText = "Err";
        }

        // Draw the opcode
        GraphicsUtil.drawCenteredText(graphics, opText, centerX, centerY);

    }

    private static String signExtend(String binaryString, int targetLength) {
        char signBit = binaryString.charAt(0);
        return String.format("%" + targetLength + "s", binaryString).replace(' ', signBit);
    }
}