import javafx.application.Application;
// import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
// import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

public class capture extends Application {
    // Start the video capture thread

    class CameraThread extends Thread {

        private final VideoCapture capture;
        private final Mat frame;
        private final ImageView imageView;
        private final int ThreadId;

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
                    if (capture.read(frame)) {

                        // Convert the OpenCV frame to a JavaFX image
                        Image image = convertMatToImage(frame);

                        // Display the image in the ImageView
                        imageView.setImage(image);
                        // imageView1.setImage(image);
                    }
                }
            }
        }

        private Image convertMatToImage(Mat mat) {
            // Convert the Mat object to a byte array
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", mat, buffer);
            byte[] imageData = buffer.toArray();

            // Convert the byte array to a JavaFX image
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            return new Image(inputStream);
        }

    }

    @Override
    public void start(Stage stage) {
        // IP/index/filename of the cameras
        String[] captures = {
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery002_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4",
                "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery004_x264.mp4" };

        // Creating Image views for the frames to be captured
        ImageView[] imageView = new ImageView[captures.length];
        for (int i = 0; i < captures.length; i++) {
            ImageView view = new ImageView();
            imageView[i] = view;
        }

        for (int i = 0; i < captures.length; i++) {
            Thread videoCaptureThread = new CameraThread(captures[i], imageView[i], i);
            videoCaptureThread.setDaemon(true);
            videoCaptureThread.start();
        }

        // Thread videoCaptureThread1 = new CameraThread(capture1, imageView1);
        // videoCaptureThread1.setDaemon(true);
        // videoCaptureThread1.start();

        stage.setTitle("Video Capture");

        HBox root = new HBox();

        // Create an array of JavaFX HBox to hold the ImageView
        HBox boxes[] = new HBox[captures.length];
        for (int i = 0; i < captures.length; i++) {
            HBox hbox = new HBox();
            boxes[i] = hbox;

        }
        for (int i = 0; i < boxes.length; i++) {

            boxes[i].getChildren().add(imageView[i]);
            root.getChildren().add(boxes[i]);
            boxes[i].setStyle("-fx-border-color:darkblue ; \n"
                    + "-fx-border-insets:3;\n"
                    + "-fx-border-radius:7;\n"
                    + "-fx-border-width:3.0;");
        }

        // Create a JavaFX Scene with the VBox and set it on the Stage
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);

    }
}