import java.io.*;
import java.util.ArrayList;

public class TxtFile {

    // create txt file
    public static void createTxtFile(String path) {
        File newF = new File(path);
        if (! newF.exists()) {
            try {
                newF.createNewFile();
            } catch (Exception e) {
                System.out.println("File Create Error.");
            }
        }
    }

    // read txt file
    public static ArrayList readTxtFile(String path) throws IOException {
        ArrayList list = new ArrayList();
        String thisLine = null;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                while ((thisLine = br.readLine()) != null) {
                    list.add(thisLine);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("File Read Error.");
            }
        }
        return list;
    }

    // write txt file
    public static void writeTxtFile(String content, String path, boolean append) {
        File thisFile = new File(path);
        try {
            if (! thisFile.exists()) {
                thisFile.createNewFile();
            }
            FileWriter fw = new FileWriter(path, append);
            fw.write(content + "\n");
            fw.close();
        } catch (Exception e) {
            System.out.println("File Write Error.");
        }
    }
}
