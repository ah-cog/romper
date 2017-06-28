package camp.computer.platform;

import camp.computer.Interpreter;

public class InterpretLineTask {

    public void execute(String inputLine) {
        Interpreter.getInstance().interpretLine(inputLine);
    }
}
