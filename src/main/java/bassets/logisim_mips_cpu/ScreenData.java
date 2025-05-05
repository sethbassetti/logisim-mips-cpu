package bassets.logisim_mips_cpu;

import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;

class ScreenData implements InstanceData, Cloneable {
    private Value lastClock;
    public BufferedImage image;
    public short[][] memory;
    private static final int COLS = 128;
    private static final int ROWS = 64;
    private boolean[][] dirtyPixels; // Track which pixels need updating
    private boolean isDirty; // Track if any updates are pending

    public static ScreenData get(InstanceState var0) {
        ScreenData var2 = (ScreenData)var0.getData();
        if (var2 == null) {
            var2 = new ScreenData((Value)null);
            var0.setData(var2);
        }
        return var2;
    }

    public ScreenData(Value clock) {
        this.lastClock = clock;
        this.memory = new short[ROWS][COLS];
        this.dirtyPixels = new boolean[ROWS][COLS];
        this.isDirty = false;
        this.image = new BufferedImage(COLS, ROWS, BufferedImage.TYPE_USHORT_565_RGB);
        Graphics graphics = this.image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.clearRect(0, 0, this.image.getWidth(), this.image.getHeight());
    }

    public Object clone() {
        try {
            ScreenData cloned = (ScreenData) super.clone();
            // Deep copy the arrays
            cloned.memory = new short[ROWS][COLS];
            cloned.dirtyPixels = new boolean[ROWS][COLS];
            for (int i = 0; i < ROWS; i++) {
                System.arraycopy(this.memory[i], 0, cloned.memory[i], 0, COLS);
                System.arraycopy(this.dirtyPixels[i], 0, cloned.dirtyPixels[i], 0, COLS);
            }
            return cloned;
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public boolean updateClock(Value value) {
        Value old = lastClock;
        lastClock = value;
        return old == Value.FALSE && value == Value.TRUE;
    }

    public Value getValue(Value address) {
        int row = address.toIntValue() / COLS;
        int col = address.toIntValue() % COLS;
        return Value.createKnown(BitWidth.create(16),image.getRGB(col, row));
    }

    public void setValue(Value inData, Value address) {
        short data = (short) inData.toIntValue();
        int row = address.toIntValue() / COLS;
        int col = address.toIntValue() % COLS;

        // Only mark as dirty if the value actually changed
        if (memory[row][col] != data) {
            memory[row][col] = data;
            dirtyPixels[row][col] = true;
            isDirty = true;
        }
    }

    /**
     * Updates the image with all pending changes in memory
     */
    public void display() {
        if (!isDirty) {
            return; // Skip if no updates are pending
        }

        // Get direct access to the image data buffer
        DataBufferUShort dataBuffer = (DataBufferUShort) image.getRaster().getDataBuffer();
        short[] imageData = dataBuffer.getData();

        // Update only the dirty pixels
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (dirtyPixels[row][col]) {
                    imageData[row * COLS + col] = memory[row][col];
                    dirtyPixels[row][col] = false;
                }
            }
        }

        isDirty = false;
    }

    /**
     * Forces a complete refresh of the image from memory
     */
    public void refreshDisplay() {
        DataBufferUShort dataBuffer = (DataBufferUShort) image.getRaster().getDataBuffer();
        short[] imageData = dataBuffer.getData();

        // Update all pixels
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                imageData[row * COLS + col] = memory[row][col];
                dirtyPixels[row][col] = false;
            }
        }

        isDirty = false;
    }
}