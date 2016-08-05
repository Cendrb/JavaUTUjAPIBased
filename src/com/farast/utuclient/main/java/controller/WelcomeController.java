package com.farast.utuclient.main.java.controller;

import com.farast.utuapi.data.DataLoader;
import com.farast.utuapi.data.Sclass;
import com.farast.utuclient.main.java.Main;
import com.farast.utuclient.main.java.util.OperationListenerLogger;
import com.farast.utuclient.main.java.util.Preferences;
import com.farast.utuclient.main.java.util.RenderingOperation;
import com.farast.utuclient.main.java.util.StatusLogger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.xml.sax.SAXException;

import java.io.IOException;

public class WelcomeController {
    @FXML
    public Label statusLabel;
    @FXML
    public ChoiceBox<Sclass> sclassSelector;

    private DataLoader dataLoader;
    private StatusLogger logger;
    private OperationListenerLogger operationLogger;

    @FXML
    public void initialize() {
        dataLoader = new DataLoader("http://localhost:3000");
        logger = new StatusLogger(statusLabel);
        operationLogger = new OperationListenerLogger(logger);

        dataLoader.getOperationManager().clearOperationListeners();
        dataLoader.getOperationManager().addOperationListener(operationLogger);

        Main.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dataLoader.loadPredata();
                    dataLoader.getOperationManager().startOperation(new RenderingOperation());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            sclassSelector.getItems().setAll(dataLoader.getSclasses());
                            sclassSelector.setConverter(new StringConverter<Sclass>() {
                                @Override
                                public String toString(Sclass object) {
                                    return object.getName();
                                }

                                @Override
                                public Sclass fromString(String string) {
                                    return null;
                                }
                            });
                            sclassSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sclass>() {
                                @Override
                                public void changed(ObservableValue<? extends Sclass> observable, Sclass oldValue, Sclass newValue) {
                                    FXMLLoader fxmlLoader = new FXMLLoader();
                                    try {
                                        Preferences.setSclassId(newValue.getId());
                                        MainController controller = new MainController(dataLoader);
                                        fxmlLoader.setController(controller);
                                        Parent mainScreen = fxmlLoader.load(getClass().getResource("/com/farast/utuclient/main/resources/view/main.fxml").openStream());
                                        mainScreen.getStylesheets().add("/com/farast/utuclient/main/resources/css/main.css");
                                        Stage stage = new Stage();
                                        stage.setScene(new Scene(mainScreen));
                                        stage.show();
                                        ((Stage) sclassSelector.getScene().getWindow()).close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            sclassSelector.setDisable(false);
                            dataLoader.getOperationManager().endOperation();
                        }
                    });
                } catch (SAXException e) {
                    logger.logException(e, "Received data is corrupted");
                } catch (IOException e) {
                    logger.logException(e, "Unable to connect to the server");
                }
            }
        });
    }
}
