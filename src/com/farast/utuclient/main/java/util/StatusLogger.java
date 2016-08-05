package com.farast.utuclient.main.java.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by cendr_000 on 28.07.2016.
 */
public class StatusLogger {
    private Label outputLabel;

    public StatusLogger(Label outputLabel) {
        this.outputLabel = outputLabel;
    }

    public void logInfo(String status) {
        Platform.runLater(() -> {
            System.out.println(status);
            outputLabel.setText(status);
        });
    }

    public void showDialog(String title, String header, String description, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(description);
            alert.showAndWait();
        });
    }

    public void showExceptionDialog(String header, String description, Exception e, boolean exitAfterDialog) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(header);
            if(exitAfterDialog) {
                alert.setTitle("Fatal error");
                alert.setContentText(description + "\nThis error is unrecoverable, application will now exit");
            }
            else
            {
                alert.setTitle("Unexpected error");
                alert.setContentText(description);
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
            if(exitAfterDialog)
                Platform.exit();
        });
    }

    public void logException(Exception e, String explanation) {
        e.printStackTrace();
        logInfo("ERROR: " + explanation + " (" + e.getClass().getName() + ")");
    }
}
