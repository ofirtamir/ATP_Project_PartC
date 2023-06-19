package View;

import Server.Configurations;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;

import static View.Log4J.LOG;

public class MyViewController implements IView {
    private int[][] maze;
    public Displayer displayer;
    private static MyViewModel viewModel;

    @FXML
    private RadioButton randomMaze, myMazeGen, bfsChoice, dfsChoice, bestChoice;
    @FXML
    private TextField threadPoolTextField;

    private enum MazeState {NOTRUNNING, RUNNING, SOLVED}
    private MazeState currentState = MazeState.NOTRUNNING;
    private int numOfSteps;
    private MediaPlayer music = null;
    private MediaPlayer fx = null;
    double currwidth;
    double currheight;
    public Stage propertiesStage = null;
    @Override
    public void setViewModel(MyViewModel viewModel, Scene scene) {
        MyViewController.viewModel = viewModel;
        scene.setOnKeyPressed(event -> {
            String codeString = event.getCode().toString();
            keyPressed(codeString);
        });
        displayerPane.setMinHeight(0);
        displayerPane.setMinWidth(0);
    }

    @Override
    public void newButton(javafx.event.ActionEvent actionEvent) {
        displayDifficultySelection();
    }

    private void displayDifficultySelection() {
        Parent root;
        Stage stage;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DifficultyMenu.fxml"));
            root = fxmlLoader.load();
            stage = new Stage();
            stage.setTitle("Select Difficulty");
            stage.setScene(new Scene(root));
            stage.show();

            DifficultyController dcontrol = fxmlLoader.getController();
            dcontrol.setParent(this, stage);
        } catch (IOException e) {
            LOG.error("Unable to display difficulty selection.", e);
        }
    }

    public Pane displayerPane;


    private void draw(Maze mazeToDraw){
        maze = new int[mazeToDraw.getRows()][mazeToDraw.getColumns()];
        for (int i = 0; i < mazeToDraw.getRows(); i++) {
            for (int j = 0; j < mazeToDraw.getColumns(); j++) {
                maze[i][j] = mazeToDraw.getVal(i, j);
            }
        }

        playTheme();
        Position pos = mazeToDraw.getStartPosition();
        maze[pos.getRowIndex()][pos.getColumnIndex()] = 2;
        pos = mazeToDraw.getGoalPosition();
        maze[pos.getRowIndex()][pos.getColumnIndex()] = 3;
        zoom(displayerPane);
        displayer.setHeight(displayerPane.getHeight());
        displayer.setWidth(displayerPane.getWidth());

        displayer.drawMaze(maze);
        if (currwidth != 0 || currheight != 0)
            redraw();
        currentState = MazeState.RUNNING;
        numOfSteps = 0;
    }
    public void drawMazeNew(int row, int col) {
        viewModel.generateMaze(row, col);
        Maze temp = viewModel.getMaze();
        LOG.info("Created a new " + temp.getRows() + "x" + temp.getColumns() +" maze.");
        draw(temp);
    }


    public void sizeListener(MyViewModel viewModel, Stage stage){
        MyViewController.viewModel = viewModel;
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newsize = newVal.doubleValue() - 15;
            newsize = Math.max(0, newsize);
            currwidth = newsize;
            redraw();
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double newsize = newVal.doubleValue() - 63;
            newsize = Math.max(0, newsize);
            currheight = newsize;
            redraw();
        });
    }

    private void redraw(){
        if(displayer.getPlayerPos()!=null){
            if (currheight != 0)
                displayer.setHeight(currheight);
            if (currwidth != 0)
                displayer.setWidth(currwidth);
            displayer.draw();
        }
    }


    private void zoom( Pane pane) {
        pane.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        if( event.isControlDown()) {
                            double zoomFactor = 1.05;
                            double deltaY = event.getDeltaY();

                            if (deltaY < 0) {
                                zoomFactor = 0.95;
                            }
                            pane.setScaleX(pane.getScaleX() * zoomFactor);
                            pane.setScaleY(pane.getScaleY() * zoomFactor);
                            event.consume();
                            LOG.debug("Zoom level changed.");
                        }
                    }
                });
    }

    @Override
    public void saveButton(javafx.event.ActionEvent actionEvent) {
        if (currentState == MazeState.NOTRUNNING) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("No maze to save.");
            alert.showAndWait();
            return;
        }
        String userResult = getUserFileName("Save Maze");
        if (userResult == null)
            return;
        if (viewModel.save(userResult)){
            successAlert("File saved successfully.");
            LOG.info(String.format("User saved a maze to file \"%s\"",userResult));
        }
        else{
            warningAlert("Unable to save the maze.");
            LOG.warn("User failed to save a maze.");
        }
    }
    @Override
    public void loadButton(javafx.event.ActionEvent actionEvent) {
        String userResult = getUserFileName("Load Maze");
        if (userResult == null)
            return;
        if (viewModel.load(userResult)) {
            successAlert("File loaded successfully.");
            LOG.info(String.format("User loaded maze \"%s\"",userResult));
            draw(viewModel.getMaze());
        }
        else {
            warningAlert("Unable to load the maze.");
            LOG.warn("User failed to load a maze.");

        }
    }
    private String getUserFileName(String title){
        TextInputDialog td = new TextInputDialog("Enter the file name...");
        td.setTitle(title);
        td.setHeaderText("File name:");
        td.showAndWait();
        return td.getResult();
    }

    private void successAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private void warningAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    //Options menu:
    @Override
    public void propertiesButton(javafx.event.ActionEvent actionEvent) {
        displayPropertiesSelection();
    }

    private void displayPropertiesSelection() {
        Parent root;
        Stage stage;
        try {
            if(propertiesStage==null) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Properties.fxml"));
                root = fxmlLoader.load();
                propertiesStage = new Stage();
                propertiesStage.setTitle("Properties Menu");
                propertiesStage.setScene(new Scene(root));
                propertiesStage.show();
            }
            else{
                propertiesStage.show();
            }
            propertiesStage.setOnCloseRequest(e -> propertiesStage.hide());
        } catch (IOException e) {
            LOG.error("Unable to display properties.", e);
            return;
        }
    }
    @Override
    public void exitButton(javafx.event.ActionEvent actionEvent) {
        LOG.info("Program terminated by user");
        viewModel.stopServers();
        Platform.exit();
        System.exit(0);
    }
    @Override
    public void helpButton(javafx.event.ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("ash ketchum needs to find Pikachu,\nUse the numpad keys to move around and find the Pikachu!\n.");
        alert.showAndWait();
    }
    @Override
    public void aboutButton(javafx.event.ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("Pikachu was set free by\nOfir Tamir\n Yeal Kanevsky");
        alert.showAndWait();
    }

    //Properties:
    public void mazeCreatePropertyChoice() {
        if (randomMaze.isSelected()) {
            Configurations.getProp().setProperty("mazeGeneratingAlgorithm", "\"SimpleMazeGenerator\"");
            LOG.info("SimpleMazeGenerator selected.");
        }
        if (myMazeGen.isSelected()) {
            Configurations.getProp().setProperty("mazeGeneratingAlgorithm", "\"MyMazeGenerator\"");
        }
        System.out.println("Current generator is: " + Configurations.getProp().getProperty("mazeGeneratingAlgorithm"));
    }

    public void solvePropertyChoice() {
        if (bfsChoice.isSelected()) {
            Configurations.getProp().setProperty("mazeSearchingAlgorithm", "\"BreadthFirstSearch\"");
            LOG.info("BreadthFirstSearch selected.");
        }
        if (dfsChoice.isSelected()) {
            Configurations.getProp().setProperty("mazeSearchingAlgorithm", "\"DepthFirstSearch\"");
            LOG.info("DepthFirstSearch selected.");
        }
        if (bestChoice.isSelected()) {
            Configurations.getProp().setProperty("mazeSearchingAlgorithm", "\"BestFirstSearch\"");
            LOG.info("BestFirstSearch selected.");
        }
        System.out.println("Current searcher is: " + Configurations.getProp().getProperty("mazeSearchingAlgorithm"));

    }

    public void threadPoolButton() {
        if (threadPoolTextField.getText().matches("-?\\d+")) {
            if(Integer.parseInt(threadPoolTextField.getText())>0) {
                Configurations.getProp().setProperty("threadPoolSize", threadPoolTextField.getText());
                LOG.info("Threadpool size changed.");
                return;
            }
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Wrong number of threads");
        alert.setHeaderText(null);
        alert.setContentText("Please input numbers above 1 in the threads text field.");
        alert.showAndWait();

        System.out.println("Current threadpool size is: " + Configurations.getProp().getProperty("threadPoolSize"));
    }



    @Override
    public void keyPressed(String keyEvent) {
        if (currentState != MazeState.RUNNING)
            return;

        boolean success;
        switch (keyEvent) {
            case "NUMPAD2" -> success = viewModel.movePlayer("DOWN");
            case "NUMPAD4" -> success = viewModel.movePlayer("LEFT");
            case "NUMPAD6" -> success = viewModel.movePlayer("RIGHT");
            case "NUMPAD8" -> success = viewModel.movePlayer("UP");
            case "NUMPAD1" -> success = viewModel.movePlayer("DOWNLEFT");
            case "NUMPAD3" -> success = viewModel.movePlayer("DOWNRIGHT");
            case "NUMPAD7" -> success = viewModel.movePlayer("UPLEFT");
            case "NUMPAD9" -> success = viewModel.movePlayer("UPRIGHT");
            default -> {return;}
        }
        if (success){
            numOfSteps++;
            Pair<Integer, Integer> pair = viewModel.getPlayerLocation();
            int result = displayer.movePlayer(pair.getKey(), pair.getValue());
            if (result == 1){
                //health pickup
                playHealth();
            }
            else if (result == 2){
                //reached goal
                playEnding();
                showScore();
                currentState = MazeState.SOLVED;
            }

        }
        else {
            playStuck();
        }
    }



    private void showScore(){
        LOG.info(String.format("User solved maze. Highscore: %s.", numOfSteps));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maze Solved");
        alert.setHeaderText("You won! You managed to reach Pikachu!");
        alert.setContentText("It only took you " + numOfSteps + " steps until you reached Pikachu!\nCan you do better?");
        alert.showAndWait();
    }

    private void playHealth(){
        if (fx != null)
            fx.stop();
        String path = "resources/Music/health.mp3";
        Media media = new Media(new File(path).toURI().toString());
        fx = new MediaPlayer(media);
        fx.setVolume(1);
        fx.setAutoPlay(true);
        fx.play();
    }
    private void playStuck() {
        if (fx != null)
            fx.stop();

        String path = "resources/Music/hitwall.mp3";
        Media media = new Media(new File(path).toURI().toString());
        fx = new MediaPlayer(media);
        fx.setVolume(1);
        fx.setAutoPlay(true);
        fx.play();
    }

    private void playEnding() {
        if (fx != null)
            fx.stop();

        String path = "resources/Music/pokeballopeningsoundFX.mp3";
        Media media = new Media(new File(path).toURI().toString());
        fx = new MediaPlayer(media);
        fx.setVolume(1);
        fx.setAutoPlay(true);
        fx.play();


        if (music != null)
            music.stop();
        path = "resources/Music/ThePikachuSong.mp3";
        media = new Media(new File(path).toURI().toString());
        music = new MediaPlayer(media);
        music.setVolume(0.3);
        music.setAutoPlay(true);

        music.setOnEndOfMedia(new Runnable() {
            public void run() {
                music.seek(Duration.ZERO);
            }
        });
        music.play();
    }

    private void playTheme(){
        if (currentState == MazeState.RUNNING)
            return;
        if (music != null)
            music.stop();
        String path = "resources/Music/PokemonThemeSong.mp3";
        Media media = new Media(new File(path).toURI().toString());
        music = new MediaPlayer(media);
        music.setVolume((0.3));
        music.setAutoPlay(true);

        music.setOnEndOfMedia(new Runnable() {
            public void run() {
                music.seek(Duration.ZERO);
            }
        });
        music.play();
    }
    @Override
    public void solveButton(ActionEvent actionEvent) {
        LOG.info("User requested solution.");
        if (currentState == MazeState.NOTRUNNING)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Solve");
            alert.setHeaderText(null);
            alert.setContentText("There's no maze yet.\nCreate a maze with File->New, or load your maze with File->Load.");
            alert.showAndWait();
            return;
        }

        if (currentState == MazeState.SOLVED)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Solve");
            alert.setHeaderText(null);
            alert.setContentText("You already solved the maze!");
            alert.showAndWait();
            return;
        }

        Pair<Integer,Integer> player = displayer.getPlayerPos();
        Pair<Integer,Integer>[] solution = viewModel.getSolution(player);
        displayer.drawSolution(solution);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (currentState != MazeState.RUNNING)
            return;
        double mouse_x = mouseEvent.getX();
        double mouse_y = mouseEvent.getY();

        dragCalculate(mouse_x, mouse_y);
    }

    private void dragCalculate(double mouse_x, double mouse_y){
        if (mouse_x < 0 || mouse_y < 0 || mouse_x > displayer.getWidth() || mouse_y > displayer.getHeight())
            return;
        int rownum = viewModel.getMaze().getRows();
        int colnum = viewModel.getMaze().getColumns();
        double relativesize_x = displayer.getWidth() / colnum;
        double relativesize_y = displayer.getHeight() / rownum;
        int pos_x = (int) (mouse_x / relativesize_x);
        int pos_y = (int) (mouse_y / relativesize_y);

        Pair<Integer, Integer> pos_p = viewModel.getPlayerLocation();
        int player_y = pos_p.getKey();
        int player_x = pos_p.getValue();

        String keypressed = "";

        if (pos_x == player_x && pos_y == player_y + 1) keypressed = "NUMPAD2"; // DOWN
        else if (pos_x == player_x - 1 && pos_y == player_y) keypressed = "NUMPAD4"; // LEFT
        else if (pos_x == player_x + 1 && pos_y == player_y) keypressed = "NUMPAD6"; // RIGHT
        else if (pos_x == player_x && pos_y == player_y - 1) keypressed = "NUMPAD8"; // UP
        else if (pos_x == player_x - 1 && pos_y == player_y + 1) keypressed = "NUMPAD1"; // DOWNLEFT
        else if (pos_x == player_x + 1 && pos_y == player_y + 1) keypressed = "NUMPAD3"; // DOWNRIGHT
        else if (pos_x == player_x - 1 && pos_y == player_y - 1) keypressed = "NUMPAD7"; // UPLEFT
        else if (pos_x == player_x + 1 && pos_y == player_y - 1) keypressed = "NUMPAD9"; // UPRIGHT

        if (!keypressed.equals(""))
            keyPressed(keypressed);
    }
}
