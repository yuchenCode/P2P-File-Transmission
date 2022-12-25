import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server {

    private Hashtable UHPT;
    private Hashtable UHRT;
    private Hashtable ipPortTable;

    private final static int SERVER_PORT = 9282;

    public Server() {
        // init tables
        UHPT = new Hashtable();
        UHRT = new Hashtable();
        ipPortTable = new Hashtable();
    }

    public void startServer() {
        ServerSocket serverSocket = null;
        try {
            // build socket for client to connect
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("server started");
        } catch (Exception e) {
            System.out.println("Server Start Error." + e);
        }
        Socket connectSocket = null;
        while (true) {
            try {
                connectSocket = serverSocket.accept();
                new ConnectHandler(connectSocket, UHPT, UHRT, ipPortTable).start();
            } catch (Exception e) {
                System.out.println("Server Connect Error." + e);
            }
        }
    }
}
