package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Controller {
    private static final String endOfMsg = "\n";

    @FXML
    private TextArea taChat;
    @FXML
    private TextField tfMessage;
    @FXML
    private Button btnSend;

    public void onActionBtnSend(ActionEvent actionEvent) {
        formatMyMsg(tfMessage.getText() + endOfMsg);
        tfMessage.clear();
        tfMessage.requestFocus();
    }

    public void onActionTfMessage(ActionEvent actionEvent) {
        onActionBtnSend(actionEvent);
    }

    private void formatMyMsg(String msg) {
        if (msg.equals(endOfMsg)) {
            return;
        }

//        TextFlow textFlow = new TextFlow();
        String formattedMsg = String.format("%s, %s:\n%s\n\n",
                "You",
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()),
                msg);
//        Text t1 = new Text(info);
//        t1.setFill(Color.BLUE);
//        Text t2 = new Text("\033[0;1m" + msg);
//        textFlow.getChildren().addAll(t1, t2);

        taChat.appendText(formattedMsg);
    }
}
