import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

public class FileHandler extends Thread{

    private Socket fileSocket;
    private Hashtable DHRT;
    private JTextArea command;

    public FileHandler(Socket fileSocket, Hashtable DHRT, JTextArea command){
        this.fileSocket = fileSocket;
        this.DHRT = DHRT;
        this.command = command;
    }

    @Override
    public void run(){
        try {
            // receive request from other clients
            InputStream inputStream = fileSocket.getInputStream();
            while (true) {
                byte[] data = new byte[1024];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    String resource = new String(data, 0, len);
                    command.append("From other client: " + resource + "\n");
                    String path = (String) DHRT.get(resource);
                    File file = new File(path);
                    // check if the file exist
                    if (file.exists()) {
                        command.append("find: " + path + "\n");
                        FileInputStream fis = new FileInputStream(file);
                        DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());

                        // write the file name and length
                        dos.writeUTF(file.getName());
                        dos.flush();
                        dos.writeLong(file.length());
                        dos.flush();

                        command.append("start transmitting file: " + resource + "\n");
                        byte[] bytes = new byte[1024];
                        int length = 0;

                        // start transmission
                        while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                            dos.write(bytes, 0, length);
                            dos.flush();
                        }
                        command.append("transmission success" + "\n");
                        fis.close();
                        dos.close();
                    } else {
                        command.append("can't find file" + "\n");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Send from Client Error."+e);
        }
    }
}
