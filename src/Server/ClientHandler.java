package Server;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private final String id;
    private final Camera camera;
    private final Socket socket;
    private final ArrayList<Boolean> control;
    private final ServerGUI serverGui;

    private BufferedImage frame;
    private DataOutputStream dos = null;

    public ClientHandler(String id, Camera camera, Socket socket, ArrayList<Boolean> control, ServerGUI serverGUI) {
        this.id = id;
        this.camera = camera;
        this.socket = socket;
        this.control = control;
        this.serverGui = serverGUI;
    }

    @Override
    public void run() {
        boolean isFrameDiff;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            Mat currentFrame = new Mat();

            while (!(!control.get(0) | dis.readBoolean())) {
                if (control.get(1)) {
                    writeDataToClient(false, false);
                    Thread.sleep(100);
                    continue;
                }

                synchronized (camera.getCameraLock()) {
                    camera.getCapture().read(currentFrame);
                }
                if (currentFrame.empty()) {
                    continue;
                }

                Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_BGR2GRAY);

                if (control.get(2)) {
                    isFrameDiff = Security.isFrameDiff();
                } else {
                    isFrameDiff = false;
                }

                frame = matToBufferedImage(currentFrame);

                writeDataToClient(isFrameDiff, false);

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException();
        } finally {
            System.err.println("> Client disconnected: " + socket);

            serverGui.removeClient(id);
            try {
                if (dos != null) {
                    writeDataToClient(false, true);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not exit client");
            }
        }
    }

    void writeDataToClient(boolean isFrameDiff, boolean shouldClose) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(frame, "jpg", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        dos.writeBoolean(shouldClose);
        dos.writeBoolean(isFrameDiff);
        dos.writeInt(imageBytes.length);
        dos.write(imageBytes, 0, imageBytes.length);
        dos.flush();

        byteArrayOutputStream.close();
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
}
