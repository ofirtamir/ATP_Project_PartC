package View;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

public class DifficultyController{

    private int row, col;

    public TextField rowsText;
    public TextField colsText;

    private MyViewController myviewc;
    private Stage stage;

    public void tooYoungDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 5;
        col = 5;
        sendBack(row, col);

    }

    public void hurtMeDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 10;
        col = 10;
        sendBack(row, col);
    }

    public void nightmareDifficulty(javafx.event.ActionEvent actionEvent) {
        row = 20;
        col = 20;
        sendBack(row, col);
    }

    public void choiceDifficulty(javafx.event.ActionEvent actionEvent) {
        if((!rowsText.getText().isEmpty()) && !(colsText.getText().isEmpty()) && rowsText.getText().matches("-?\\d+") && colsText.getText().matches("-?\\d+")) {
            row = Integer.parseInt(rowsText.getText());
            col = Integer.parseInt(colsText.getText());
            sendBack(row, col);
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("YOU CAN'T CHEAT YOUR WAY IN");
            alert.setHeaderText(null);
            alert.setContentText("These aren't numbers!\nEnter only full numbers in the columns and rows fields!");
            alert.showAndWait();
        }
    }

    public void sendBack(int row, int col){
        stage.close();
        myviewc.drawMazeNew(row, col);

    }

    public void setParent(MyViewController myViewController, Stage stage) {
        myviewc = myViewController;
        this.stage = stage;
    }
}
