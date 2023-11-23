package Server;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Camera {
    private final VideoCapture capture;
    private Object cameraLock;


    public Camera(int device) {
        capture = new VideoCapture(device);
        if (!capture.isOpened()) {
            System.err.println("<error> Could not open the selected camera");
            return;
        }
        cameraLock = new Object();
    }

    public VideoCapture getCapture() {
        return capture;
    }

    public Object getCameraLock() {
        return cameraLock;
    }
}
