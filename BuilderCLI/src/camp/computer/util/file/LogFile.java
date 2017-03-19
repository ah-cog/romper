package camp.computer.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogFile {

    private BufferedWriter writer = null;

    private File file = null;

    public LogFile() {

        try {

            // Create temporary log file
            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//            file = new File("log_" + timeLog + ".txt");
            file = new File("log.txt");

            writer = new BufferedWriter(new FileWriter(file));

            // This will output the full path where the file will be written to...
            System.out.println("Logging in " + file.getCanonicalPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String line) {

        try {

            writer.write(line + "\r\n");
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                // Close the writer regardless of what happens...
//                writer.close();
//            } catch (Exception e) {
//            }
//        }

    }

}
