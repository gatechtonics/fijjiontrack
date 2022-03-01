import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Controller extends Application {
        boolean file;
        boolean roi;
        boolean count;
        boolean distance2d;
        boolean distance3d;
        public static void main(String[] args) {
            launch(args);
        }
        @Override
        public void start(Stage primaryStage) {
            try {
                primaryStage.setTitle("Fission Track Plugins!!!");
                Scene scene = initStartScreen();
                primaryStage.setScene(scene);
                primaryStage.show();
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        private Scene initStartScreen() {
            file = false;
            roi = false;
            count = false;
            distance2d = false;
            distance3d = false;
            StartScreen startScreen = new StartScreen();
            return startScreen.getScene();
        }

}
