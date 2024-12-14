import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;

public class NextFitSimulation extends Application {

    // list views for displaying memory and processes
    private ListView<String> memoryListView;
    private ListView<String> processListView;
    // buttons for next step, submit, close, and reset
    private Button nextButton;
    private Button submitButton;
    private Button closeButton;
    private Button resetButton;
    // text fields for user input
    private TextField blockInput;
    private TextField processInput;

    // arrays for block sizes, process sizes, and allocations
    private int[] blockSize;
    private int[] processSize;
    private int[] allocation;
    // variables for tracking current process and block
    private int currentProcess = 0;
    private int currentBlock = 0;
    private int lastAllocatedBlock = -1;

    @Override
    public void start(Stage stage) {
        // initialize list views
        memoryListView = new ListView<>();
        processListView = new ListView<>();
        // initialize buttons
        nextButton = new Button("Next Step");
        submitButton = new Button("Submit Input");
        closeButton = new Button("Close");
        resetButton = new Button("Reset");
        // initialize text fields
        blockInput = new TextField();
        processInput = new TextField();

        // Set button styles
        nextButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        submitButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        closeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        resetButton.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");

        // set up input section for user
        VBox inputBox = new VBox(10);
        inputBox.setStyle("-fx-padding: 10px;"); // add padding around input box
        inputBox.getChildren().addAll(
                new Label("Enter memory block sizes in KB (comma separated):"), blockInput,
                new Label("Enter process sizes in KB (comma separated):"), processInput,
                submitButton
        );

        // set action for submit button
        submitButton.setOnAction(e -> handleSubmitInput());

        // set action for reset button
        resetButton.setOnAction(e -> resetSimulation());

        // set action for close button
        closeButton.setOnAction(e -> stage.close());

        // set action for next step button
        nextButton.setOnAction(e -> performNextStep());
        nextButton.setDisable(true); // disable initially

        // layout for memory and processes display
        VBox mainLayout = new VBox(10);
        mainLayout.setStyle("-fx-padding: 10px;"); // add padding around the main content
        mainLayout.getChildren().addAll(new Label("Memory Blocks:"), memoryListView,
                new Label("Processes:"), processListView);

        // HBox for action buttons aligned horizontally at the bottom
        HBox buttonLayout = new HBox(10, nextButton, resetButton, closeButton);
        buttonLayout.setStyle("-fx-alignment: center; -fx-padding: 10px;");

        // set up the main layout and scene
        VBox rootLayout = new VBox(15);
        rootLayout.setStyle("-fx-padding: 20px;"); // add padding around the outer layout
        rootLayout.getChildren().addAll(inputBox, mainLayout, buttonLayout);

        Scene scene = new Scene(rootLayout, 500, 600);
        stage.setTitle("Next Fit Memory Allocation Simulation");
        stage.setScene(scene);
        stage.show();
    }

    // method for handling user input after clicking submit
    private void handleSubmitInput() {
        try {
            // get input from text fields
            String blockInputText = blockInput.getText();
            String processInputText = processInput.getText();

            // parse block sizes and process sizes from input text
            blockSize = Arrays.stream(blockInputText.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();

            processSize = Arrays.stream(processInputText.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();

            // initialize allocation array
            allocation = new int[processSize.length];
            Arrays.fill(allocation, -1); // all unallocated initially
            currentProcess = 0;
            lastAllocatedBlock = -1;

            // update memory and process views
            updateMemoryView();
            updateProcessView();

            // enable the next step button
            nextButton.setDisable(false);

            // disable submit button after input is taken
            submitButton.setDisable(true);

        } catch (Exception e) {
            // handle invalid input
            memoryListView.getItems().clear();
            processListView.getItems().clear();
            memoryListView.getItems().add("Invalid input. Please enter valid numbers.");
            processListView.getItems().add("Invalid input. Please enter valid numbers.");
        }
    }

    // method for performing the next step in the simulation
    private void performNextStep() {
        if (currentProcess < processSize.length) {
            boolean allocated = false;

            // search for a block to allocate the current process
            for (int i = 0; i < blockSize.length; i++) {
                currentBlock = (lastAllocatedBlock + 1 + i) % blockSize.length;
                if (blockSize[currentBlock] >= processSize[currentProcess]) {
                    // allocate process to block
                    allocation[currentProcess] = currentBlock;
                    blockSize[currentBlock] -= processSize[currentProcess];
                    lastAllocatedBlock = currentBlock;
                    allocated = true;
                    break;
                }
            }

            // if not allocated, show a popup
            if (!allocated) {
                showAlert("Not Enough Space", "Process " + (currentProcess + 1) + " (" +
                        processSize[currentProcess] + " KB) cannot be allocated to any block.");
            }

            // update views after allocation attempt
            updateMemoryView();
            updateProcessView();

            // move to next process
            currentProcess++;
        }

        // disable next button if all processes are handled
        if (currentProcess >= processSize.length) {
            nextButton.setDisable(true);
        }
    }

    // method to display an alert popup
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // method to update the memory list view
    private void updateMemoryView() {
        memoryListView.getItems().clear();
        for (int i = 0; i < blockSize.length; i++) {
            memoryListView.getItems().add("Block " + (i + 1) + ": " + blockSize[i] + " KB (Free)");
        }
    }

    // method to update the process list view
    private void updateProcessView() {
        processListView.getItems().clear();
        for (int i = 0; i < processSize.length; i++) {
            if (allocation[i] != -1) {
                processListView.getItems().add("Process " + (i + 1) + ": " + processSize[i] +
                        " KB -> Block " + (allocation[i] + 1));
            } else {
                processListView.getItems().add("Process " + (i + 1) + ": " + processSize[i] + " KB (Unallocated)");
            }
        }
    }

    // method to reset the simulation and go back to input stage
    private void resetSimulation() {
        // clear all inputs and reset internal states
        blockInput.clear();
        processInput.clear();
        memoryListView.getItems().clear();
        processListView.getItems().clear();
        nextButton.setDisable(true);
        submitButton.setDisable(false);
        currentProcess = 0;
        lastAllocatedBlock = -1;
        allocation = null;
        blockSize = null;
        processSize = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
