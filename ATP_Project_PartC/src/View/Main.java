package View;

import Model.*;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Objects;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MyView.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Pokemon-Maze");
        Scene scene = new Scene(root, 400, 420);
        scene.getStylesheets().addAll(Objects.requireNonNull(this.getClass().getResource("style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        MyModel model = new MyModel();
        model.connectGenerator(5400, 1000, new ServerStrategyGenerateMaze());
        model.connectSolver(5401, 1000, new ServerStrategySolveSearchProblem());

        MyViewModel viewModel = new MyViewModel(model);
        MyViewController myview = fxmlLoader.getController();
        myview.setViewModel(viewModel, scene);
        myview.sizeListener(viewModel,primaryStage);
        primaryStage.setOnCloseRequest( e-> myview.exitButton(new ActionEvent()));

    }


    public static void main(String[] args) {
        launch(args);
    }
}
