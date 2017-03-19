package camp.computer;

import camp.computer.util.file.LogFile;

public class Application {

    public static LogFile log = new LogFile();

    public static void main(String[] args) {

        // TODO: Create different devices, including Clay, IR rangefinder, PWM servo, Arduino

        // TODO: Load extensions from file
        // TODO: Load extensions from Redis

        // TODO: Create projects by constructing paths

        // TODO: Save/Load/IASM for Projects

        System.out.println("Builder (Version 0.4.5)");

        Interpreter.getInstance().start();

    }
}
