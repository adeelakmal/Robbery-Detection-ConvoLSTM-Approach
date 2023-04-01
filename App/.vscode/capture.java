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
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

import java.net.*;
import java.io.*;

public class capture extends Application {

    // IP/index/filename of the cameras
    static String[] sources = {
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery002_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery004_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery015_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery016_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery017_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery018_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery019_x264.mp4" };
    // Creating Imageviews and captures for the frames to be captured
    VideoCapture[] captures = new VideoCapture[sources.length];
    ImageView[] imageView = new ImageView[sources.length];
    Rectangle[] borders = new Rectangle[sources.length];

    private static final int NUM_SOURCES = sources.length; // Number of video sources
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

        return image;
    }

    @Override
    public void start(Stage stage) {
        try {

            // To establish connection with Python server
            String serverName = "127.0.0.1";
            int port = 8000;

            Socket clientSocket = new Socket(serverName, port);
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();

            // Sending a message to server
            String message = "Hello from the client!";
            outputStream.write(message.getBytes());

            clientSocket.close();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // VideoCapture[] captures = new VideoCapture[sources.length];
        for (int i = 0; i < sources.length; i++) {
            captures[i] = new VideoCapture(sources[i]);
        }

        for (int i = 0; i < sources.length; i++) {
            ImageView view = new ImageView();
            view.setFitHeight(144);
            view.setFitWidth(256);

            Rectangle border = new Rectangle(256, 144, Color.TRANSPARENT);
            border.setStroke(Color.BLUE);
            border.setStrokeWidth(2);

            imageView[i] = view;
            borders[i] = border;
        }

        // Start the video capture threads
        for (int i = 0; i < NUM_SOURCES; i++) {
            final int index = i;
            executor.scheduleAtFixedRate(() -> {
                // Capture the frame
                Mat frame = new Mat(640, 480, CvType.CV_8UC3);
                if (captures[index].read(frame)) {

                    // System.out.println(frame.get(10, 10)[0]);

                    // Convert the frame to an image
                    Image image = convertMatToImage(frame);

                    // Update the image view
                    Platform.runLater(() -> imageView[index].setImage(image));
                }

            }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
        }

        stage.setTitle("Video Capture");

        // Putting all the ImageViews inside a 3x3 grid by checking the total amount in
        // the captures array
        int TOTAL_CAMERAS = sources.length;
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

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);

    }
}