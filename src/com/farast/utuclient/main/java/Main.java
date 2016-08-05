package com.farast.utuclient.main.java;

import com.farast.utuapi.data.DataLoader;
import com.farast.utuclient.main.java.controller.MainController;
import com.farast.utuclient.main.java.util.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private static ExecutorService executor;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        executor = Executors.newSingleThreadExecutor();
        FXMLLoader fxmlLoader = new FXMLLoader();
        if (Preferences.getSclassId() != -1) {
            DataLoader dataLoader = new DataLoader("http://localhost:3000");
            MainController controller = new MainController(dataLoader);
            fxmlLoader.setController(controller);
            Parent mainScene = fxmlLoader.load(getClass().getResource("/com/farast/utuclient/main/resources/view/main.fxml").openStream());
            mainScene.getStylesheets().add("/com/farast/utuclient/main/resources/css/main.css");
            primaryStage.setTitle("UTU");
            primaryStage.setScene(new Scene(mainScene, 1500, 700));
            primaryStage.show();
        } else {
            Parent sclassSelect = fxmlLoader.load(getClass().getResource("/com/farast/utuclient/main/resources/view/welcome.fxml").openStream());
            primaryStage.setTitle("UTU");
            primaryStage.setScene(new Scene(sclassSelect, 300, 275));
            primaryStage.show();
        }

        System.out.println();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static ExecutorService getExecutor() {
        return executor;
    }
}
