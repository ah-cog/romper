package camp.computer.construct;

import camp.computer.util.terminal.Color;

public class Error {

    // TODO: Make Error extend Type

    enum Type {
        ERROR,
        WARNING,
        INFO
    }

    public String message = null;

    private Error(String message) {
        this.message = Color.ANSI_RED + "Error: " + message + Color.ANSI_RESET;
    }

    public static Error get(String message) {
        return new Error(message);
    }

    @Override
    public String toString() {
        return this.message;
    }
}
