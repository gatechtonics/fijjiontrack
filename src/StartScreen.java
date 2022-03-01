import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class StartScreen {
    public Scene getScene() {
        Button btn1 = new Button();
        btn1.setText("File");
        Button btn2 = new Button();
        btn2.setText("ROI");
        Button btn3 = new Button();
        btn3.setText("Count");
        Button btn4 = new Button();
        btn4.setText("2D_Distance");
        Button btn5 = new Button();
        btn5.setText("3D_Distance");
        BorderPane bp = new BorderPane();
        HBox hbox = new HBox();
        hbox.getChildren().addAll(btn1, btn2, btn3, btn4, btn5);
        Label distance2d = new Label("2D Distance");
        TextArea text2d = new TextArea("area to store 2d distance");
        Label distance3d = new Label("3D Distance");
        TextArea text3d = new TextArea("area to store 3d distance");
        distance3d.setId("label3d");
        distance2d.setId("label2d");
        VBox vbox = new VBox();
        vbox.getChildren().addAll(distance2d, text2d, distance3d, text3d);
        bp.setTop(hbox);
        bp.setLeft(vbox);
        Label brightness = new Label("Brightness");
        Label contrast = new Label("Contrast");
        Label sharpness = new Label("Sharpness");
        Button reflectiveLight = new Button("ReflectiveLight");

        Label count = new Label("Count");
        count.setId("count");
        Text num = new Text("num");
        num.setId("num");
        HBox countHbox = new HBox(count, num);
        countHbox.setSpacing(10);

        VBox rightVbox = new VBox();
        rightVbox.getChildren().addAll(brightness, contrast, sharpness, reflectiveLight, countHbox);
        bp.setRight(rightVbox);


        Scene scene = new Scene(bp, 600, 400);
        scene.getStylesheets().add("file:src/css/style.css");
        return scene;

    }
}
