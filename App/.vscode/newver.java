import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.net.*;

import java.io.*;

public class newver extends Application {

    public class FrameObject {
        // private int frameId;
        private int sourceId;
        private byte[] frameData;

        public FrameObject(int sourceId, byte[] frameData) {
            // this.frameId = frameId;
            this.sourceId = sourceId;
            this.frameData = frameData;
        }

        public int getSource() {
            return sourceId;
        }

        public byte[] getFrames() {
            return frameData;
        }
    }

    // To establish connection with Python server
    String serverName = "127.0.0.1";
    int port = 8000;

    private Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    // IP/index/filename of the cameras
    static String[] sources = {
            /* "http://@172.30.66.173:8080/video", */
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery002_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery004_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery015_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery016_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery017_x264.mp4",
    };

    static int[] sources1 = { 0 };
    // Creating Imageviews and captures for the frames to be captured
    static VideoCapture[] captures = new VideoCapture[sources.length + sources1.length];
    ImageView[] imageView = new ImageView[sources.length + sources1.length];
    Rectangle[] borders = new Rectangle[sources.length + sources1.length];

    private static final int NUM_SOURCES = sources.length + sources1.length; // Number of video sources
    private static final int FRAME_RATE = 25; // Frame rate of the video

    // Executor to create seperate threads for frame capture
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_SOURCES);

    // Maybe remove this function because it is causing issues in frame capture
    private Image convertMatToImage(Mat mat) {
        // Convert the Mat object to a byte array
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);

        // Convert the byte array to a JavaFX image
        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
        // Code to send pixel data to python

        buffer = new MatOfByte(); // Free buffer from the space
        return image;
    }

    @Override
    public void start(Stage stage) {

        try {
            clientSocket = new Socket(serverName, port);
            outputStream = clientSocket.getOutputStream();
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5));
        grid.setHgap(5);
        grid.setVgap(5);

        for (int i = 0; i < sources.length; i++) {
            captures[i] = new VideoCapture(sources[i]);
        }
        for (int i = 0; i < sources1.length; i++) {
            captures[sources.length + i] = new VideoCapture(sources1[i]);
        }

        for (int i = 0; i < sources.length; i++) {
            ImageView view = new ImageView();
            view.setFitHeight(144);
            view.setFitWidth(256);

            Rectangle border = new Rectangle(256, 144, Color.TRANSPARENT);
            // border.setStroke(Color.BLUE);
            // if (i == 6) {
            // border.setStroke(Color.RED);
            // }
            // border.setStrokeWidth(2);

            imageView[i] = view;
            borders[i] = border;
        }
        for (int i = 0; i < sources1.length; i++) {
            ImageView view = new ImageView();
            view.setFitHeight(144);
            view.setFitWidth(256);

            Rectangle border = new Rectangle(256, 144, Color.TRANSPARENT);
            // border.setStroke(Color.BLUE);
            // border.setStrokeWidth(2);

            imageView[sources.length + i] = view;
            borders[sources.length + i] = border;
        }

        // Start the video capture threads
        for (int i = 0; i < NUM_SOURCES; i++) {
            final int index = i;
            executor.scheduleAtFixedRate(() -> {
                // Capture the frame
                Mat frame = new Mat(240, 320, CvType.CV_8UC3);
                // byte[][] capturedFrames = new byte[10][];

                if (captures[index].read(frame)) {
                    // Resizing image to 100 X 100px
                    Mat resizedFrame = new Mat();
                    Imgproc.resize(frame, resizedFrame, new Size(100, 100));
                    frame.release();

                    int height = resizedFrame.rows();
                    int width = resizedFrame.cols();
                    System.out.println("Height: " + height + " width: " + width);
                    // Convert the frame to an image
                    Image image = convertMatToImage(resizedFrame);

                    // Update the image view
                    Platform.runLater(() -> imageView[index].setImage(image));

                    // Converting mat to a byte array

                    byte[] data = new byte[resizedFrame.channels() * width * height];
                    resizedFrame.get(0, 0, data);

                    // FrameObject frameObj = new FrameObject(index, data);

                    // capturedFrames[index] = data;

                    // if (capturedFrames.length >= 10) {

                    // try {
                    // // objectMapper = new ObjectMapper();
                    // // String json = objectMapper.writeValueAsString(frameObj);
                    // // byte[] jsonBytes = json.getBytes("UTF-8");
                    // // System.out.println("THIS IS THE JSON " + jsonBytes);
                    // // outputStream.write(index);
                    // outputStream.write(Integer.toString(index).getBytes("UTF-8"));
                    // outputStream.flush();
                    // outputStream.write(data);
                    // outputStream.flush();

                    // } catch (IOException e) {
                    // e.printStackTrace();
                    // }
                    // // }

                    // Remove the frame object from memory
                    resizedFrame.release();

                    // Add shutdown hook to close socket client
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            clientSocket.close();
                            outputStream.close();
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }));

                }

            }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
        }

        stage.setTitle("Video Capture");

        // Putting all the ImageViews inside a 3x3 grid by checking the total amount in
        // the captures array
        int TOTAL_CAMERAS = sources.length + sources1.length;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (TOTAL_CAMERAS > 0) {
                    TOTAL_CAMERAS--;
                    grid.add(imageView[TOTAL_CAMERAS], i, j);
                    grid.add(borders[TOTAL_CAMERAS], i, j);
                }
            }

        }

        // Create a JavaFX Scene with the VBox and set it on the Stage
        Scene scene = new Scene(grid, 820, 480);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Stop the video capture threads
        executor.shutdownNow();

        // Release the video captures
        for (VideoCapture capture : captures) {
            capture.release();
        }

        // Shutdown OpenCV
        System.exit(0);
    }

    public static void main(String args[]) {
        System.loadLibrary("opencv_videoio_ffmpeg470_64");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}