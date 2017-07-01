import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class IWasFramed  extends Application {
    private final static Logger logger = Logger.getLogger(IWasFramed.class.getName());

    private final Label videoFileLabel = new Label("Video File");
    private final FileChooser chooser = new FileChooser();
    private final TextField videoFile = new TextField("");

    private final Label startTimeLabel = new Label("Start From");
    private final TextField startTime = new TextField("00:00:00.000");

    private final Label endTimeLabel = new Label("End At");
    private final TextField endTime = new TextField("99:59:59.999");

    private final Label stepLabel = new Label("Step Length (ms)");
    private final TextField stepLength = new TextField("5");

    private final Label cleanupLabel = new Label("Delete Locals");
    private final CheckBox cleanup = new CheckBox();

    private final Button selectFileButton = new Button("Select File");
    private final Button ruploadButton = new Button("Rupload");
    private final Button stopButton = new Button("Stop");
    private final Button exitButton = new Button("Exit");

    private Future videoThread = null;
    private ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(final Stage stage) {
        stage.setTitle("I Was Framed");

        File videosDirectory = new File("./Videos/");
        if (!videosDirectory.exists()) videosDirectory.mkdirs();

        fieldsSetup(videosDirectory, stage);

        stage.setScene(createLayout());
        stage.show();
    }

    private void fieldsSetup(File videosDirectory, Stage stage) {
        startTime.setEditable(false);
        startTime.setDisable(true);

        endTime.setEditable(false);
        endTime.setDisable(true);

        stepLength.setEditable(false);
        stepLength.setDisable(true);

        videoFile.setPrefSize(250,20);
        videoFile.setEditable(false);
        videoFile.setDisable(true);

        boolean cleanupFilesAfterCreation = true;
        cleanup.setSelected(cleanupFilesAfterCreation);
        Utils.setCleanUp(cleanupFilesAfterCreation);
        cleanup.selectedProperty().addListener((arg, oldVal, newVal) -> Utils.setCleanUp(newVal));

        Utils.getSharedProgressBar().setDisable(true);
        Utils.getSharedProgressBar().setPrefWidth(400);

        chooser.setInitialDirectory(videosDirectory);
        chooser.setTitle("Select Video Clip");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.*"),
                new FileChooser.ExtensionFilter("AVI", "*.avi"),
                new FileChooser.ExtensionFilter("MP4", "*.mp4"),
                new FileChooser.ExtensionFilter("MOV", "*.mov"),
                new FileChooser.ExtensionFilter("WMV", "*.wmv")
        );

        selectFileButton.setOnAction((event) -> {
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                populateFile(file);
                ruploadButton.setDisable(false);
            }
        });

        int prefWidth = 100;
        ruploadButton.setDisable(true);
        ruploadButton.setOnAction((event) -> {
            logger.info("running...");
            Utils.getSharedProgressBar().setDisable(false);
            
            logger.info("new thread");

            Utils.getSharedProgressBar().setProgress(0);

            if (videoThread != null) threadPoolExecutor.shutdownNow();

            threadPoolExecutor = Executors.newSingleThreadExecutor();
            videoThread = threadPoolExecutor.submit(new VideoProcessor(videoFile.getText(), Integer.parseInt(stepLength.getText()), Optional.of(Utils.getMillis(startTime.getText())), Optional.of(Utils.getMillis(endTime.getText()))));
        });
        ruploadButton.setPrefWidth(prefWidth);

        stopButton.setOnAction((event) -> {
            logger.info("stopping...");

            if (videoThread != null) threadPoolExecutor.shutdownNow();
        });
        stopButton.setPrefWidth(prefWidth);

        exitButton.setOnAction((event) -> {
            if (videoThread != null) threadPoolExecutor.shutdownNow();

            System.exit(0);
        });
        exitButton.setPrefWidth(prefWidth);
    }

    private void populateFile(File file) { if (file != null) enableFields(file);  }

    private void enableFields(File file) {
        videoFile.setText(file.getAbsolutePath());
        startTime.setEditable(true);
        startTime.setDisable(false);

        endTime.setEditable(true);
        endTime.setDisable(false);

        stepLength.setEditable(true);
        stepLength.setDisable(false);
    }

    private Scene createLayout() {
        BorderPane parentPane = new BorderPane();
        parentPane.setPadding(new Insets(15, 15, 15, 15));

        parentPane.setTop(getVideoSelectionPane());
        parentPane.setCenter(getProgressPane());
        parentPane.setBottom(getExecutionPane());

        return new Scene(parentPane);
    }

    private GridPane getVideoSelectionPane() {
        final GridPane videoSelectionPane = new GridPane();
        GridPane.setConstraints(videoFileLabel, 0, 0);
        GridPane.setConstraints(videoFile, 1, 0);
        GridPane.setConstraints(selectFileButton, 2, 0);
        GridPane.setConstraints(startTimeLabel, 0, 1);
        GridPane.setConstraints(startTime, 1, 1);
        GridPane.setConstraints(endTimeLabel, 0, 2);
        GridPane.setConstraints(endTime, 1, 2);
        GridPane.setConstraints(stepLabel, 0, 3);
        GridPane.setConstraints(stepLength, 1, 3);
        GridPane.setConstraints(cleanupLabel, 0, 4);
        GridPane.setConstraints(cleanup, 1, 4);

        videoSelectionPane.setHgap(6);
        videoSelectionPane.setVgap(6);
        videoSelectionPane.getChildren().addAll(videoFileLabel, videoFile, startTimeLabel, startTime, endTimeLabel, endTime, stepLabel, stepLength, selectFileButton, cleanupLabel, cleanup);
        return videoSelectionPane;
    }

    private Pane getProgressPane() {
        final FlowPane progressPane = new FlowPane();

        progressPane.setPadding(new Insets(20, 0, 0,0));
        progressPane.setHgap(6);
        progressPane.setVgap(6);

        progressPane.getChildren().add(Utils.getSharedProgressBar());
        progressPane.setAlignment(Pos.CENTER);

        return progressPane;
    }

    private GridPane getExecutionPane() {
        final GridPane executionPane = new GridPane();
        GridPane.setConstraints(ruploadButton, 0, 4);
        GridPane.setConstraints(stopButton, 1, 4);
        GridPane.setConstraints(exitButton, 2, 4);

        executionPane.setHgap(6);
        executionPane.setVgap(6);

        executionPane.getChildren().addAll(ruploadButton, stopButton, exitButton);
        executionPane.setAlignment(Pos.BOTTOM_CENTER);

        return executionPane;
    }
}
