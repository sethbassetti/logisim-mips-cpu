package bassets.logisim_mips_cpu;

import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Components extends Library {
    private final List<? extends Tool> tools = Arrays.asList(
            new AddTool(new ALU()),
            new AddTool(new Inc()),
            new AddTool(new Screen()),
            new AddTool(new RegisterFile()),
            new AddTool(new Control()),
            new AddTool(new CPU()),
            new AddTool(new Keyboard())
    );

    @Override
    public List<? extends Tool> getTools() {
        return tools;
    }

    @Override
    public String getDisplayName() {
        return "AggieMIPS Components";
    }
}