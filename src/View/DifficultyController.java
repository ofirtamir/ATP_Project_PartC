package View;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

public class DifficultyController{

    private int row, col;

    public TextField rowsInput;
    public TextField colsInput;

    private MyViewController myViewCon;
    private Stage stage;

    public void EasyDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 5;
        col = 5;
        sendBack(row, col);

    }

    public void MediumDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 10;
        col = 10;
        sendBack(row, col);
    }

    public void HardDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 20;
        col = 20;
        sendBack(row, col);
    }

    public void choiceDifficulty(javafx.event.ActionEvent actionEvent) {
        if((!rowsInput.getText().isEmpty()) && !(colsInput.getText().isEmpty()) && rowsInput.getText().matches("-?\\d+") && colsInput.getText().matches("-?\\d+")) {
            row = Integer.parseInt(rowsInput.getText());
            col = Integer.parseInt(colsInput.getText());
            sendBack(row, col);
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Wrong Input");
            alert.setHeaderText("These aren't numbers!");
            alert.setContentText("Please enter only full numbers in the columns and rows fields!");
            alert.showAndWait();
        }
    }

    public void sendBack(int row, int col){
        stage.close();
        myViewCon.drawMazeNew(row, col);

    }

    public void setParent(MyViewController myViewController, Stage stage) {
        myViewCon = myViewController;
        this.stage = stage;
    }
}
