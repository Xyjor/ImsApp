/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package imsapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main entry point for the Vehicle &amp; Driver IMS application.
 *
 * @author Xyjor
 */
public class ImsApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Locate the Dashboard FXML resource
        URL fxmlUrl = getClass().getResource("/imsapp/view/LoginView.fxml");
        if (fxmlUrl == null) {
            // Fallback: try relative path (same package)
            fxmlUrl = getClass().getResource("view/LoginView.fxml");
        }
        if (fxmlUrl == null) {
            System.err.println("ERROR: Could not find LoginView.fxml on the classpath.");
            System.err.println("Make sure you do a full Clean & Build (not Run Single File).");
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root, 600, 400);

        // Load CSS stylesheet
        URL cssUrl = getClass().getResource("/imsapp/css/style.css");
        if (cssUrl == null) {
            cssUrl = getClass().getResource("css/style.css");
        }
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Vehicle & Driver Information Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Close the database connection on application exit
        imsapp.util.DBConnection.closeConnection();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
