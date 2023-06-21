package View;

import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

public interface IView {
    void setViewModel(MyViewModel viewModel, Scene scene);

    void newButton(javafx.event.ActionEvent actionEvent);

    void saveButton(javafx.event.ActionEvent actionEvent);

    void loadButton(javafx.event.ActionEvent actionEvent);

    //Options menu:
    void propertiesButton(javafx.event.ActionEvent actionEvent);

    void exitButton(javafx.event.ActionEvent actionEvent);

    void helpButton(javafx.event.ActionEvent actionEvent);

    void aboutButton(javafx.event.ActionEvent actionEvent);

    void keyPressed(String keyEvent);

    void solveButton(ActionEvent actionEvent);

    void mouseDragged(MouseEvent mouseEvent);
}
