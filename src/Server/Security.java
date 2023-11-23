package Server;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoWriter;
import org.opencv.imgproc.Imgproc;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class Security implements Runnable {

    private final int device;
    private final Camera camera;
    private boolean register = false;
    private VideoWriter videoWriter;
    private static boolean frameDiff = false;

    public Security(int device, Camera camera) {
        this.device = device;
        this.camera = camera;
    }

    @Override
    public void run() {
        double threshold = 50.0;

        AtomicInteger index = new AtomicInteger(1);
        int fourcc = VideoWriter.fourcc('X', 'V', 'I', 'D');
        int fps = 24;
        Size frameSize = new Size(640, 480);

        Mat previousFrame = new Mat();
        Mat currentFrame = new Mat();

        while (camera.getCapture().isOpened()) {

            synchronized (camera.getCameraLock()) {
                camera.getCapture().read(currentFrame);
            }

            Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_BGR2GRAY);

            if (previousFrame.empty()) {
                previousFrame = currentFrame.clone();
            }

            frameDiff = calculateMSE(previousFrame, currentFrame) > threshold;

            if (!register & frameDiff) {
                startRecording(index.getAndIncrement(), fourcc, fps, frameSize);
            }

            if (register) {
                videoWriter.write(currentFrame);
            }
            HighGui.imshow("Video Recording", currentFrame);

            previousFrame = currentFrame.clone();
        }

        videoWriter.release();
    }

    void startRecording(int index, int fourcc, int fps, Size frameSize) {
        videoWriter = new VideoWriter(buildName(index), fourcc, fps, frameSize);
        register = true;
        new Thread(() -> {
            try {
                Thread.sleep(25000);
            } catch (InterruptedException ignored) {
            }
            register = false;
            videoWriter.release();
        }).start();
    }

    private double calculateMSE(Mat frame1, Mat frame2) {
        Mat diff = new Mat();
        Core.absdiff(frame1, frame2, diff);
        Mat squaredDiff = new Mat();
        Core.multiply(diff, diff, squaredDiff);
        Scalar mse = Core.mean(squaredDiff);

        return mse.val[0];
    }

    private String buildName(int index) {
        return "recordings/camera_" + device + "_danger_" + index + "_at_" + getTimeNow() + ".mp4";
    }

    private String getTimeNow() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH.mm.ss");
        return currentTime.format(formatter);
    }

    public static boolean isFrameDiff() {
        return frameDiff;
    }

}
