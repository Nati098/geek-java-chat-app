package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class RegController {
    @FXML
    public TextField tfLogin;
    @FXML
    public PasswordField pfPassword;
    @FXML
    public TextField tfNickname;
    @FXML
    public TextArea textArea;

    private Controller controller;


    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void appendToTextArea(String msg) {
        textArea.appendText(msg + "\n");
    }

    public void onActionBtnReg(ActionEvent actionEvent) {
        controller.register(tfLogin.getText().trim(),
                pfPassword.getText().trim(),
                tfNickname.getText().trim());
    }


}
