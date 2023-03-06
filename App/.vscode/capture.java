import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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

        VideoCapture capture = new VideoCapture();
        // VideoCapture capture = new
        // VideoCapture("http://172.30.44.175:8080/shot.jpg");
        // VideoCapture capture = new VideoCapture(0);

        @Override
        public void run() {
            // capture.open("http://172.30.75.213:8080/video");
            capture.open(
                    "E:/Uni Assignments/SEMESTER 7/COMP 497A/DATASET/rebuilt.Anomaly-Videos-Part-3/Anomaly-Videos-Part-3/Robbery/Robbery001_x264.mp4");
            Mat frame = new Mat();
            while (true) {
                if (capture.read(frame)) {
                    // Convert the OpenCV frame to a JavaFX image
                    Image image = convertMatToImage(frame);

                    // Display the image in the ImageView
                    imageView.setImage(image);
                    imageView1.setImage(image);
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
        Thread videoCaptureThread = new CameraThread();
        videoCaptureThread.setDaemon(true);
        videoCaptureThread.start();

        Thread videoCaptureThread1 = new CameraThread();
        videoCaptureThread1.setDaemon(true);
        videoCaptureThread1.start();

        stage.setTitle("Video Capture");

        // Create a JavaFX VBox to hold the ImageView
        VBox vbox = new VBox();
        vbox.getChildren().add(imageView);
        vbox.getChildren().add(imageView1);

        // Create a JavaFX Scene with the VBox and set it on the Stage
        Scene scene = new Scene(vbox, 640, 480);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);

    }
}