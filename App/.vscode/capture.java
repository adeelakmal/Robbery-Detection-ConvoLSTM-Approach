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
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class capture extends Application {

    // To establish connection with Python server
    String serverName = "127.0.0.1";
    int port = 5000;

    private Socket clientSocket;
    private OutputStream outputStream;
    // private InputStream inputStream;

    // IP/index/filename of the cameras
    static String[] sources = {
            // /* "http://@192.168.0.108:8080/video", */

            // "rtsp://admin:csproject2023@10.16.14.66/ISAPI/Streaming/Channels/101/picture",
            // "rtsp://admin:csproject2023@10.16.14.67/ISAPI/Streaming/Channels/101/picture",
            // "rtsp://admin:csproject2023@10.16.14.69/ISAPI/Streaming/Channels/101/picture",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery002_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery004_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery015_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery016_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery016_x264.mp4",
            // "E:/Uni Assignments/SEMESTER 7/COMP
            // 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery016_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Testing_Normal_Videos_Anomaly/Normal_Videos_006_x264.mp4",
            "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Testing_Normal_Videos_Anomaly/Normal_Videos_003_x264.mp4",

    };

    // static int lenSources = 5;
    // static int lenSources1 = 1;

    static int[] sources1 = { 0 };
    // Creating Imageviews and captures for the frames to be captured
    static VideoCapture[] captures = new VideoCapture[9];
    ImageView[] imageView = new ImageView[9];
    Rectangle[] borders = new Rectangle[9];

    GridPane grid = new GridPane();

    // has the predictions for every source
    private String[] results = new String[sources.length + sources1.length];
    private String defaultVal = "Normal";

    private static final int NUM_SOURCES = sources.length + sources1.length; // Number of video sources
    private static final int FRAME_RATE = 25; // Frame rate of the video

    // Executor to create seperate threads for frame capture
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_SOURCES);
    private static final ScheduledExecutorService resultExecutor = Executors.newSingleThreadScheduledExecutor();

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

        Arrays.fill(results, defaultVal);

        try {
            clientSocket = new Socket(serverName, port);
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            imageView[i] = view;

            Rectangle border = new Rectangle(256, 144, Color.TRANSPARENT);

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
                    // System.out.println("Height: " + height + " width: " + width);
                    // Convert the frame to an image
                    Image image = convertMatToImage(resizedFrame);

                    // Update the image view
                    Platform.runLater(() -> imageView[index].setImage(image));

                    // Converting mat to a byte array
                    // if (isMatImageFull(resizedFrame)) {

                    ScheduledExecutorService innerScheduler = Executors.newSingleThreadScheduledExecutor();

                    innerScheduler.scheduleAtFixedRate(() -> {
                        byte[] data = new byte[resizedFrame.channels() * width * height];
                        resizedFrame.get(0, 0, data);

                        byte[] packet = new byte[4 + data.length];
                        ByteBuffer.wrap(packet).putInt(index).put(data);

                        try {

                            outputStream.write(packet);
                            outputStream.flush();

                        } catch (IOException e) {
                            // e.printStackTrace();
                        }

                        // Remove the frame object from memory
                        resizedFrame.release();
                        innerScheduler.shutdown();

                    }, 0, 500, TimeUnit.MILLISECONDS);

                    // }

                    // Add shutdown hook to close socket client
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            clientSocket.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }));

                }

            }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
        }

        Runnable readFileTask = () -> {
            // Read file for information on the results
            String fileName = "E:/Uni Assignments/SEMESTER 8/FYP/Robbery-Detection-ConvoLSTM-Approach/myfile.txt";
            File file = new File(fileName);

            // Check if the file is not empty
            if (file.length() > 0) {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    String line;
                    int lineNo = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        results[lineNo] = line;
                        lineNo++;
                    }
                    bufferedReader.close();
                    fileReader.close();

                    // Clear the file
                    PrintWriter writer = new PrintWriter(file);
                    writer.print("");
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            int index = 0;
            for (String element : results) {

                System.out.println(element);

                if (element.equals("Robbery")) {
                    borders[index].setStroke(Color.RED);
                    borders[index].setStrokeWidth(2);
                    index++;
                } else if (element.equals("Warning")) {
                    borders[index].setStroke(Color.YELLOW);
                    borders[index].setStrokeWidth(2);
                    index++;
                } else {
                    borders[index].setStroke(Color.GREEN);
                    borders[index].setStrokeWidth(2);
                    index++;
                }
            }
            System.out.println(index);
        };

        resultExecutor.scheduleAtFixedRate(readFileTask, 0, 1000, TimeUnit.MILLISECONDS);

        stage.setTitle("Video Capture");

        // Putting all the ImageViews inside a 3x3 grid by checking the total amount in
        // the captures

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