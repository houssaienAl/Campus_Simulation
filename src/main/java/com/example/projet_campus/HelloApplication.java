package com.example.projet_campus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Login.fxml")
        );
        Scene loginScene = new Scene(loader.load(), 700, 600);

        // pass the stage into the controller so it can swap scenes
        HomeController homeCtrl = loader.getController();
        homeCtrl.setStage(stage);

        stage.setTitle("Campus Simulator — Login");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
