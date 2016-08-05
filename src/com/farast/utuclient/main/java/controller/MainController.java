package com.farast.utuclient.main.java.controller;

import com.farast.utuapi.data.*;
import com.farast.utuapi.util.CollectionUtil;
import com.farast.utuapi.util.DateFormatException;
import com.farast.utuapi.util.DateUtil;
import com.farast.utuapi.util.SclassDoesNotExistException;
import com.farast.utuclient.main.java.Main;
import com.farast.utuclient.main.java.util.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by cendr_000 on 26.07.2016.
 */
public class MainController {

    static ArrayList<ColumnConstraints> columnConstraints = new ArrayList<>();
    static ArrayList<RowConstraints> rowConstraints = new ArrayList<>();

    static {
        // cols
        ColumnConstraints titleColumn = new ColumnConstraints();
        titleColumn.setMinWidth(190);
        titleColumn.setMaxWidth(190);
        columnConstraints.add(titleColumn);
        ColumnConstraints rightFloatColumn = new ColumnConstraints();
        rightFloatColumn.setMinWidth(60);
        rightFloatColumn.setMaxWidth(60);
        columnConstraints.add(rightFloatColumn);

        // rows
        RowConstraints titleRow = new RowConstraints();
    }

    @FXML
    public ListView<TEItem> tasks;
    @FXML
    public ListView<TEItem> exams;
    @FXML
    public ListView<Event> events;
    @FXML
    public Label statusLabel;
    @FXML
    public TabPane timetableTabPane;
    @FXML
    public MenuItem reloadMenuItem;
    @FXML
    public GridPane infoGrid;
    @FXML
    public GridPane rakingsGrid;

    private DataLoader dataLoader;

    private OperationListenerLogger operationLogger;
    private StatusLogger logger;

    private HashMap<Integer, Pane> timetableCells = new HashMap<>();

    public MainController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @FXML
    public void initialize() {
        events.setCellFactory(param -> new EventCell());
        exams.setCellFactory(param -> new TECell());
        tasks.setCellFactory(param -> new TECell());

        logger = new StatusLogger(statusLabel);
        operationLogger = new OperationListenerLogger(logger);

        dataLoader.getOperationManager().clearOperationListeners();
        dataLoader.getOperationManager().addOperationListener(operationLogger);

        reload();
    }

    public void wipeDefaultSclass() {
        try {
            Preferences.setSclassId(-1);
            ((Stage) tasks.getScene().getWindow()).close();
            Parent sclassSelect = FXMLLoader.load(getClass().getResource("/com/farast/utuclient/main/resources/view/welcome.fxml"));
            Stage stage = new Stage();
            stage.setTitle("UTU");
            stage.setScene(new Scene(sclassSelect, 300, 275));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        reloadMenuItem.setDisable(true);
        Main.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!dataLoader.isPredataLoaded()) {
                        dataLoader.loadPredata();
                    }

                    dataLoader.load(Preferences.getSclassId());

                    dataLoader.getOperationManager().startOperation(new RenderingOperation());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            events.getItems().setAll(dataLoader.getEventsList());
                            exams.getItems().setAll(dataLoader.getExamsList());
                            tasks.getItems().setAll(dataLoader.getTasksList());

                            timetableTabPane.getTabs().clear();
                            timetableCells.clear();
                            List<Timetable> timetables = dataLoader.getTimetablesList();
                            for (Timetable timetable : timetables) {
                                Tab tab = new Tab();
                                tab.setText(timetable.getName());
                                List<String> groupNames = timetable.getValidSgroups().stream().map(new Function<Sgroup, String>() {
                                    @Override
                                    public String apply(Sgroup sgroup) {
                                        return sgroup.getName();
                                    }
                                }).collect(Collectors.toCollection(ArrayList::new));
                                tab.setTooltip(new Tooltip("Rozvrh pro skupiny: " + String.join(", ", groupNames)));
                                GridPane timetableGrid = new GridPane();
                                List<SchoolDay> days = timetable.getSchoolDays();
                                int rowIndex = 0;
                                for (SchoolDay day : days) {
                                    Label dateLabel = new Label(DateUtil.CZ_WEEK_DATE_FORMAT.format(day.getDate()));
                                    timetableGrid.add(dateLabel, 0, rowIndex);

                                    List<Lesson> lessons = day.getLessons();
                                    for (int colIndex = 1; colIndex < 9; colIndex++) {
                                        VBox lessonVBox = new VBox();
                                        lessonVBox.setPrefWidth(50);
                                        lessonVBox.setPrefHeight(60);
                                        final int finalColIndex = colIndex;
                                        ArrayList<Lesson> filtered = CollectionUtil.filter(lessons, lesson -> lesson.getSerialNumber() == finalColIndex);
                                        if (filtered.size() > 0) {
                                            Lesson lesson = filtered.get(0);

                                            HBox hbSubject = new HBox();
                                            hbSubject.setAlignment(Pos.CENTER);
                                            Label subject = new Label(lesson.getSubject().getName());
                                            subject.setFont(new Font(16));
                                            subject.setTextAlignment(TextAlignment.CENTER);
                                            hbSubject.getChildren().add(subject);
                                            lessonVBox.getChildren().add(hbSubject);

                                            HBox hbTeacher = new HBox();
                                            hbTeacher.setAlignment(Pos.CENTER);
                                            Label teacher = new Label(lesson.getTeacher().getAbbr());
                                            teacher.setFont(new Font(12));
                                            teacher.setTextAlignment(TextAlignment.CENTER);
                                            hbTeacher.getChildren().add(teacher);
                                            lessonVBox.getChildren().add(hbTeacher);

                                            lessonVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("#EEEEEE"), CornerRadii.EMPTY, Insets.EMPTY)));
                                            timetableCells.put(lesson.getId(), lessonVBox);
                                        } else {
                                            // cell is empty
                                            //lessonVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), CornerRadii.EMPTY, Insets.EMPTY)));
                                        }
                                        //lessonVBox.setStyle("-fx-border-color: #000000;");
                                        timetableGrid.add(lessonVBox, colIndex, rowIndex);
                                    }
                                    rowIndex++;
                                }
                                tab.setContent(timetableGrid);
                                timetableTabPane.getTabs().add(tab);
                            }
                            dataLoader.getOperationManager().endOperation();
                            reloadMenuItem.setDisable(false);
                        }
                    });
                } catch (NumberFormatException | DateFormatException | CollectionUtil.MultipleRecordsWithSameIdException | CollectionUtil.RecordNotFoundException | SAXException e) {
                    operationLogger.logException(e, "Received data is corrupted", "Incompatible data received, try updating, sorry for the inconvenience", true, dataLoader.getOperationManager().getCurrentOperation());
                } catch (IOException e) {
                    operationLogger.logException(e, "Unable to connect to the server", "Check your internet connection and try again", true, dataLoader.getOperationManager().getCurrentOperation());
                } catch (SclassDoesNotExistException e) {
                    operationLogger.logException(e, "Requested class doesn't exist, wiping saved class id", "Select a different class please", false, dataLoader.getOperationManager().getCurrentOperation());
                    wipeDefaultSclass();
                }
            }
        });
    }

    private class EventCell extends ListCell<Event> {
        @Override
        protected void updateItem(Event item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                GridPane grid = new GridPane();
                grid.setMaxWidth(250);
                grid.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        infoGrid.getChildren().clear();

                        infoGrid.add(BullshitFactory.newLabel(item.getTitle(), "infobox_title"), 0, 0);
                        Label descLabel = new Label(item.getDescription());
                        descLabel.setWrapText(true);
                        descLabel.setTextAlignment(TextAlignment.JUSTIFY);
                        descLabel.setMaxWidth(300);
                        infoGrid.add(descLabel, 0, 1);
                        infoGrid.add(new Label(item.getLocation()), 0, 2);
                        infoGrid.add(new Label(DateUtil.CZ_DATE_FORMAT.format(item.getStart()) + " - " + DateUtil.CZ_DATE_FORMAT.format(item.getStart())), 0, 3);
                        infoGrid.add(new Label(String.valueOf(item.getPrice())), 0, 4);
                        infoGrid.add(new Label(item.getSgroup().getName()), 0, 5);

                    }
                });
                grid.getColumnConstraints().addAll(columnConstraints);
                grid.setHgap(20);
                grid.add(BullshitFactory.newLabel(item.getTitle(), "maingrid_title"), 0, 0);
                grid.add(BullshitFactory.newLabel(item.getDescription(), "maingrid_description"), 0, 1);
                if (item.getStart().equals(item.getEnd())) {
                    grid.add(BullshitFactory.newLabel(DateUtil.CZ_SHORT_DATE_FORMAT.format(item.getStart()), "maingrid_date"), 1, 1);
                } else {
                    grid.add(BullshitFactory.newLabel(DateUtil.CZ_SHORT_DATE_FORMAT.format(item.getStart()), "maingrid_date"), 1, 0);
                    grid.add(BullshitFactory.newLabel(DateUtil.CZ_SHORT_DATE_FORMAT.format(item.getEnd()), "maingrid_date"), 1, 1);
                }
                setGraphic(grid);
            }
        }
    }

    private class TECell extends ListCell<TEItem> {
        @Override
        protected void updateItem(TEItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                GridPane grid = new GridPane();
                setMaxWidth(250);
                grid.setMaxWidth(250);
                grid.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        infoGrid.getChildren().clear();

                        infoGrid.add(BullshitFactory.newLabel(item.getTitle(), "infobox_title"), 0, 0);
                        Label descLabel = BullshitFactory.newLabel(item.getDescription(), "infobox_description");
                        descLabel.setWrapText(true);
                        descLabel.setTextAlignment(TextAlignment.JUSTIFY);
                        descLabel.setMaxWidth(300);
                        infoGrid.add(descLabel, 0, 1);
                        infoGrid.add(BullshitFactory.newLabel(DateUtil.CZ_DATE_FORMAT.format(item.getDate()), "infobox_date"), 0, 2);
                        infoGrid.add(BullshitFactory.newLabel(item.getSgroup().getName(), "infobox_sgroup_name"), 0, 5);

                        // lessons highlighting
                        for (Pane pane : timetableCells.values())
                            pane.setBackground(new Background(new BackgroundFill(Paint.valueOf("#EEEEEE"), CornerRadii.EMPTY, Insets.EMPTY)));
                        for (Lesson lesson : item.getLessons())
                            (timetableCells.get(lesson.getId())).setBackground(new Background(new BackgroundFill(Paint.valueOf("yellow"), CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                });
                grid.getColumnConstraints().addAll(columnConstraints);
                grid.setHgap(20);
                grid.add(BullshitFactory.newLabel(item.getTitle(), "maingrid_title"), 0, 0);
                grid.add(BullshitFactory.newLabel(item.getSubject().getName(), "maingrid_subject"), 1, 0);
                grid.add(BullshitFactory.newLabel(item.getDescription(), "maingrid_description"), 0, 1);
                grid.add(BullshitFactory.newLabel(DateUtil.CZ_SHORT_DATE_FORMAT.format(item.getDate()), "maingrid_date"), 1, 1);
                setGraphic(grid);
            }
        }
    }
}
