package Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private boolean
            alarmDelayFlag = true,
            movementFlag = false,
            serverCloseClientFlag = false,
            exitButtonFlag = false;

    private DataInputStream dis;
    private DataOutputStream dos;
    private BufferedImage image;
    private AudioEffect audioEffect;
    private Thread soundTimeout;

    public Client(String ip_addr, int device) {
        try (Socket socket = new Socket(ip_addr, 2505)) {
            JFrame frame = new JFrame("Camera index " + device);
            JLabel label = new JLabel();

            clientGUI(frame, label);

            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(device);

            dis = new DataInputStream(socket.getInputStream());

            while (true) {
                dos.writeBoolean(exitButtonFlag);
                readServerData();

                if (serverCloseClientFlag) {
                    frame.dispose();
                    break;
                }

                if (movementFlag & alarmDelayFlag) {
                    audioEffect = new AudioEffect("sounds/sound.wav");
                    delay();
                }

                label.setIcon(new ImageIcon(image));
                frame.pack();
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        } finally {
            if (audioEffect != null) {
                audioEffect.stopSound();
                soundTimeout.interrupt();
            }
            try {
                if (dos != null & !serverCloseClientFlag) {
                    dos.writeBoolean(exitButtonFlag);
                    System.exit(0);
                }
            } catch (IOException e) {
                System.out.println("Client closed");
            }
        }
    }

    void readServerData() throws IOException {
        serverCloseClientFlag = dis.readBoolean();
        movementFlag = dis.readBoolean();
        int imageSize = dis.readInt();
        byte[] imageBytes = new byte[imageSize];
        dis.readFully(imageBytes, 0, imageSize);
        image = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
    }

    void delay() {
        alarmDelayFlag = false;
        soundTimeout = new Thread(() -> {
            try {
                Thread.sleep(10000);
                alarmDelayFlag = true;
            } catch (InterruptedException ignored) {}
        });
        soundTimeout.start();
    }

    private void clientGUI(JFrame frame, JLabel label) {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(640, 480);

        frame.add(label);
        frame.setVisible(true);
        frame.setIconImage(new ImageIcon("images/camera.jpg").getImage());

        WindowListener windowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitButtonFlag = true;
                frame.dispose();
            }
        };

        frame.addWindowListener(windowListener);
    }
}
