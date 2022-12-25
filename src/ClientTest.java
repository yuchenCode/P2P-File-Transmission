import javax.swing.*;
import java.awt.*;

public class ClientTest {
    public static void main(String[] args) {

        new Thread(() -> {
            // create Frame
            JFrame frame = new JFrame("P2P Client1");
            // Setting the width and height of frame
            frame.setSize(620, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // create Panel
            JPanel panel = new JPanel();
            // add Panel
            frame.add(panel);

            // set component
            placeComponents(panel, "client1");
            frame.setVisible(true);
            frame.setResizable(false);
        }).start();

        new Thread(() -> {
            // create Frame
            JFrame frame = new JFrame("P2P Client2");
            // Setting the width and height of frame
            frame.setSize(620, 500);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // create Panel
            JPanel panel = new JPanel();
            // add Panel
            frame.add(panel);

            // set component
            placeComponents(panel, "client2");
            frame.setVisible(true);
            frame.setResizable(false);
        }).start();
    }

    private static void placeComponents(JPanel panel, String name) {

        // create command line display
        JTextArea command = new JTextArea(20, 50);
        command.setBounds(40, 150, 260, 280);
        panel.add(command);

        // create progress bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setBounds(340, 300,220, 25);
        bar.setForeground(Color.green);
        panel.add(bar);

        // create client
        Client client = new Client(name, command, bar);
        panel.setLayout(null);

        // create ip Label
        JLabel ipLabel = new JLabel("IP Address:", JLabel.CENTER);
        ipLabel.setBounds(40,40,80,25);
        panel.add(ipLabel);

        // create ip textField
        JTextField ipText = new JTextField(20);
        ipText.setBounds(140,40,160,25);
        ipText.setText("127.0.0.1");
        panel.add(ipText);

        // create port Label
        JLabel portLabel = new JLabel("Port:", JLabel.CENTER);
        portLabel.setBounds(40,90,80,25);
        panel.add(portLabel);

        // create port textField
        JTextField portText = new JTextField(20);
        portText.setBounds(140,90,160,25);
        if (name == "client1") {
            portText.setText("9283");
        } else if (name == "client2") {
            portText.setText("9284");
        }
        panel.add(portText);

        // create connect button
        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(340, 65, 100, 25);
        panel.add(connectButton);
        connectButton.addActionListener((actionEvent -> {
            try {
                if (ipText.getText() != "" && portText.getText() != "") {
                    client.connect(ipText.getText().trim(), Integer.parseInt(portText.getText().trim()));
                    client.fileServerThread.start();
                }
            } catch (Exception e) {
                System.out.println("Main Error." + e);
            }
        }));

        // create disconnect button
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.setBounds(460, 65, 100, 25);
        panel.add(disconnectButton);
        disconnectButton.addActionListener((actionEvent -> {
            try {
                client.disConnect();
            } catch (Exception e) {
                System.out.println("Main Error." + e);
            }
        }));

        // create share button
        JButton shareButton = new JButton("Share a file");
        shareButton.setBounds(370, 150, 160, 25);
        panel.add(shareButton);
        shareButton.addActionListener((actionEvent -> {
            try {
                client.shareRes();
            } catch (Exception e) {
                System.out.println("Main Error." + e);
            }
        }));

        // create md5 Label
        JLabel md5Label = new JLabel("MD5", JLabel.CENTER);
        md5Label.setBounds(340,220,40,25);
        panel.add(md5Label);

        // create md5 textField
        JTextField md5Text = new JTextField(20);
        md5Text.setBounds(400,220,160,25);
        panel.add(md5Text);

        // create download button
        JButton downloadButton = new JButton("Download");
        downloadButton.setBounds(400, 380, 100, 25);
        panel.add(downloadButton);
        downloadButton.addActionListener((actionEvent -> {
            try {
                if (md5Text.getText() != "") {
                    client.getRes(md5Text.getText());
                }
            } catch (Exception e) {
                System.out.println("Main Error." + e);
            }
        }));
    }
}