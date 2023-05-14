import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
// import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

public class oldver extends Application {
    // Start the video capture thread

    class CameraThread extends Thread {

        private volatile VideoCapture capture;
        private volatile Mat frame;
        private volatile ImageView imageView;
        private volatile int ThreadId;

        public CameraThread(int capture, ImageView imageView, int ThreadId) {
            this.capture = new VideoCapture(capture);
            this.frame = new Mat();
            this.imageView = imageView;
            this.ThreadId = ThreadId;

        }

        public CameraThread(String capture, ImageView imageView, int ThreadId) {
            this.capture = new VideoCapture(capture);
            this.frame = new Mat();
            this.imageView = imageView;
            this.ThreadId = ThreadId;
        }

        @Override
        public void run() {

            while (true) {
                synchronized (this) {
                    frame = new Mat();
                    if (capture.read(frame)) {

                        // Convert the OpenCV frame to a JavaFX image
                        Image image = convertMatToImage(frame);

                        // Display the image in the ImageView
                        // imageView.setImage(image);
                        Platform.runLater(() -> imageView.setImage(image));
                    } else {
                        System.err.println("Error: could not read frame from capture device.");
                        break;
                    }

                }
            }
            capture.release();

        }

        // Maybe remove this function because it is causing issues in frame capture
        private Image convertMatToImage(Mat mat) {
            // Convert the Mat object to a byte array
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", mat, buffer);
            // Convert the byte array to a JavaFX image
            Image image = new Image(new ByteArrayInputStream(buffer.toArray()));

            return image;
        }

    }

    @Override
    public void start(Stage stage) {

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);
        // IP/index/filename of the cameras
        String[] captures = {
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery002_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery004_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery005_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery006_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery007_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery008_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery009_x264.mp4" };

        // Creating Image views for the frames to be captured
        ImageView[] imageView = new ImageView[captures.length];
        Rectangle[] borders = new Rectangle[captures.length];
        for (int i = 0; i < captures.length; i++) {
            ImageView view = new ImageView();
            view.setFitHeight(144);
            view.setFitWidth(256);

            Rectangle border = new Rectangle(256, 144, Color.TRANSPARENT);
            border.setStroke(Color.RED);
            border.setStrokeWidth(2);

            imageView[i] = view;
            borders[i] = border;
        }

        Thread[] threads = new CameraThread[captures.length];
        for (int i = 0; i < captures.length; i++) {
            threads[i] = new CameraThread(captures[i], imageView[i], i);
            threads[i].setDaemon(true);
            threads[i].start();
        }

        stage.setTitle("Video Capture");

        // Putting all the ImageViews inside a 3x3 grid by checking the total amount in
        // the captures array
        int TOTAL_CAMERAS = captures.length;
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

        // try {
        // for (int i = 0; i < captures.length; i++) {
        // threads[i].join();
        // }
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
    }

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);

    }
}