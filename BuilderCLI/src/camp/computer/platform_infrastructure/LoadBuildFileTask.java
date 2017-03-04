package camp.computer.platform_infrastructure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoadBuildFileTask {

    public void execute(String filePath) {

        FileReader in = null;
        try {
            in = new FileReader(filePath);
            BufferedReader br = new BufferedReader(in);
            String inputLine = br.readLine();
            while (inputLine != null) {
                new InterpretLineTask().execute(inputLine);
                inputLine = br.readLine();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
