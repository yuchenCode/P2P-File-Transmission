import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;

public class Client{

    private String name;
    private JTextArea command;
    private JProgressBar bar;
    private Socket clientSocket;
    private Hashtable DHRT;
    private String path;

    private final static String SERVER_IP = "127.0.0.1";
    private final static int SERVER_PORT = 9282;
    private String ip;
    private int port;

    public Client(String name, JTextArea command, JProgressBar bar) {
        // init client name and JTextArea
        this.name = name;
        this.command = command;
        this.bar = bar;
    }

    public void connect(String ip, int port) {
        try {
            // store local ip and port
            this.ip = ip;
            this.port = port;
            // connect server and upload ip and port
            clientSocket = new Socket(SERVER_IP, SERVER_PORT);
            Writer Writer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
            Writer.write(port + "");
            Writer.flush();
            Writer.write(ip);
            Writer.flush();
            command.append("client online: " + ip + " " + port + "\n");
            // choose local DHRT
            int result = 0;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            result = fileChooser.showOpenDialog(fileChooser);
            if (result == JFileChooser.APPROVE_OPTION) {
                this.path = fileChooser.getSelectedFile().getAbsolutePath() + File.separatorChar + name;
            }
            TxtFile.createTxtFile(path);
            // read local txt DHRT file
            ArrayList list = TxtFile.readTxtFile(path);
            DHRT = new Hashtable();
            for (int i = 0; i < list.size(); i+=2) {
                DHRT.put(list.get(i), list.get((i+1)));
                // upload local DHRT
                this.updateResInfo("s" + list.get(i));
            }
        } catch (Exception e) {
            System.out.println("Client Connect Error." + e);
        }
    }

    public void updateResInfo(String resource) {
        try {
            // upload md5 of shared file
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
            writer.write(resource);
            writer.flush();
            command.append("client shares: " + resource + "\n");
        } catch (Exception e) {
            System.out.println("Client Update Error." + e);
        }
    }

    public void receiveResInfo(String resource) {
        try {
            InputStream inputStream = clientSocket.getInputStream();
//            clientSocket.setSoTimeout(10 * 1000);
            while (true) {
                byte[] data = new byte[1024];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    // receive the String format of another client's ip and port
                    String ipPortS = new String(data, 0, len);
                    String ip = ipPortS.substring(4);
                    int port = Integer.parseInt(ipPortS.substring(0, 4));
                    command.append("client receive: " + ip + " " + port + "\n");
                    // build socket connect
                    Socket socket = new Socket(ip, port);
                    Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    writer.write(resource);
                    writer.flush();

                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    // read file name and length
                    String fileName = dis.readUTF();
                    long fileLength = dis.readLong();
                    // choose file saved directory
                    int result = 0;
                    String path = null;
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    result = fileChooser.showOpenDialog(fileChooser);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        path = fileChooser.getSelectedFile().getAbsolutePath();
                    }
                    File file = new File(path + File.separatorChar + fileName);

                    // display receive information
                    FileOutputStream fos = new FileOutputStream(file);
                    command.append("file     : " + file + "\n");
                    command.append("fileName : " + fileName + "\n");

                    // write the file in local
                    command.append("start receiving file: " + resource + "\n");
                    byte[] bytes = new byte[1024];
                    int length = 0;
                    int count = 0;
                    while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                        count += 1024;
                        int progress = (int) (count / fileLength) * 100;
                        bar.setString(progress + "%");
                        bar.setValue(progress);
                        fos.write(bytes, 0, length);
                        fos.flush();
                    }
                    // close the stream and socket
                    command.append("receiving success" + "\n");
                    dis.close();
                    fos.close();
                    socket.close();
                    break;
                }
                break;
            }
        } catch (Exception e) {
            System.out.println("Client Receive Error." + e);
        }
    }

    public void shareRes() {
        try {
            // choose file for share
            int result = 0;
            String path = null;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            result = fileChooser.showOpenDialog(fileChooser);
            if (result == JFileChooser.APPROVE_OPTION) {
                path = fileChooser.getSelectedFile().getAbsolutePath();
                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                // generate md5 (GUID) of the shared file
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] buffer = new byte[1024];
                int length = -1;
                while ((length = fis.read(buffer, 0, 1024)) != -1) {
                    md.update(buffer, 0, length);
                }
                BigInteger bigInt = new BigInteger(1, md.digest());
                // write shared file in local DHRT
                DHRT.put(bigInt.toString(16), path);
                // write into txt file
                TxtFile.writeTxtFile(bigInt.toString(16), this.path, true);
                TxtFile.writeTxtFile(path, this.path, true);
                this.updateResInfo("s" + bigInt.toString(16));
            }
        } catch (Exception e) {
            System.out.println("Client Share Error." + e);
        }
    }

    public void getRes(String resource) {
        try {
            // send request for file
            System.out.println(resource);
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
            writer.write("g" + resource);
            writer.flush();
            command.append("client gets: " + resource + "\n");
            this.receiveResInfo(resource);
        } catch (Exception e) {
            System.out.println("Client Get Error." + e);
        }
    }

    Thread fileServerThread = new Thread() {
        public void run() {
            ServerSocket serverSocket = null;
            try {
                // build socket for server to share file to other clients
                serverSocket = new ServerSocket(port);
                command.append("file server started" + "\n");
            } catch (Exception e) {
                System.out.println("Server Socket from Client Error." + e);
            }
            Socket fileSocket = null;
            while (true) {
                try {
                    // wait for other clients to connect
                    fileSocket = serverSocket.accept();
                    new FileHandler(fileSocket, DHRT, command).start();
                } catch (Exception e) {
                    System.out.println("Server Thread from Client Start Error." + e);
                }
            }
        }
    };

    public void disConnect() {
        try {
            // close socket
            clientSocket.close();
            command.append("client offline" + "\n");
        } catch (Exception e) {
            System.out.println("Client Disconnect Error." + e);
        }
    }
}
