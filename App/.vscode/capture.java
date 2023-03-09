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

    // Make the imageView accessible to all the threads
    volatile public ImageView imageView = new ImageView();
    volatile public ImageView imageView1 = new ImageView();

    class CameraThread extends Thread {

        private final VideoCapture capture;
        private final Mat frame;
        private final ImageView imageView;

        public CameraThread(int capture, ImageView imageView) {
            this.capture = new VideoCapture(capture);
            this.frame = new Mat();
            this.imageView = imageView;

        }

        public CameraThread(String capture, ImageView imageView) {
            this.capture = new VideoCapture(capture);
            this.frame = new Mat();
            this.imageView = imageView;
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

        // Start the video capture thread
        String capture1 = "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4";
        String capture = "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery003_x264.mp4";

        Thread videoCaptureThread = new CameraThread(capture, imageView);
        videoCaptureThread.setDaemon(true);
        videoCaptureThread.start();

        Thread videoCaptureThread1 = new CameraThread(capture1, imageView1);
        videoCaptureThread1.setDaemon(true);
        videoCaptureThread1.start();

        stage.setTitle("Video Capture");

        HBox root = new HBox();

        // Create a JavaFX VBox to hold the ImageView
        HBox vbox = new HBox();
        HBox vbox1 = new HBox();
        vbox.getChildren().add(imageView);
        vbox1.getChildren().add(imageView1);
        // vbox1.translateXProperty();
        root.getChildren().add(vbox);
        root.getChildren().add(vbox1);
        vbox.setStyle("-fx-border-color:darkblue ; \n"
                + "-fx-border-insets:3;\n"
                + "-fx-border-radius:7;\n"
                + "-fx-border-width:3.0;");

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