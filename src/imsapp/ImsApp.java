package imsapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

/**
 * Main entry point — Vehicle & Driver IMS.
 * Launches in borderless fullscreen (undecorated + maximized).
 */
public class ImsApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL fxmlUrl = getClass().getResource("/imsapp/view/LoginView.fxml");
        if (fxmlUrl == null) fxmlUrl = getClass().getResource("view/LoginView.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERROR: LoginView.fxml not found on classpath.");
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        // Use screen bounds so the scene fills the entire display
        javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());

        // Load CSS
        URL cssUrl = getClass().getResource("/imsapp/css/style.css");
        if (cssUrl == null) cssUrl = getClass().getResource("css/style.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        // Borderless fullscreen: remove OS title bar, then maximize
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Vehicle & Driver Information Management System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        imsapp.util.DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
