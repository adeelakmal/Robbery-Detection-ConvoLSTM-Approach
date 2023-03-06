
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class App {

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // create two instances of the VideoCapture class for two cameras
        VideoCapture camera1 = new VideoCapture(0);

        if (!camera1.isOpened()) {
            System.out.println("Cannot open cameras");
            return;
        }

        Mat frame1 = new Mat();

        while (true) {
            // capture a frame from camera 1
            camera1.read(frame1);
            Imgproc.cvtColor(frame1, frame1, Imgproc.COLOR_BGR2GRAY);
            Imgcodecs.imwrite("camera1.jpg", frame1);

        }

        // camera1.release();

    }
}
