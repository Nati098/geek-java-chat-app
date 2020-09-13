package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.CustomServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static data.Commands.*;


public class Controller implements Initializable {
    private final String IP_ADDRESS = "localhost";
    private final String TITLE = "Chatty";
    private final String endOfMsg = "\n";

    @FXML
    public BorderPane bpAuth;
    @FXML
    public TextField tfLogin;
    @FXML
    public PasswordField pfPassword;

    @FXML
    public BorderPane bpMessage;
    @FXML
    public TextArea taChat;
    @FXML
    public TextField tfMessage;

    @FXML
    public ListView<String> clientList;

    private Stage regStage;
    private RegController regController;

    private Socket socket;
    DataInputStream dis;
    DataOutputStream dos;

    private boolean isAuthenticated;
    private String nickname;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAuthenticated(false);
        createRegWindow();
        Platform.runLater(() -> {
            Stage stage = (Stage) tfMessage.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("Goodbye!");
                if (socket != null && !socket.isClosed()) {
                    try {
                        dos.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;

        bpAuth.setVisible(!isAuthenticated);
        bpAuth.setManaged(!isAuthenticated);
        bpMessage.setVisible(isAuthenticated);
        bpMessage.setManaged(isAuthenticated);
        clientList.setVisible(isAuthenticated);
        clientList.setManaged(isAuthenticated);

        if (!isAuthenticated) {
            nickname = "Sign In";
        }

        taChat.clear();
        if (isAuthenticated) {
            taChat.appendText(formatMsg("Server:\nWelcome!\n"));
        }

        setTitle(nickname);
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Sign Up");
            regStage.setScene(new Scene(root, 400, 250));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, CustomServer.PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String msg = dis.readUTF();
                        if (msg.startsWith(AUTHOK.getCommand())) {
                            nickname = msg.split(" ", 2)[1];  // /authok nick
                            setAuthenticated(true);
                            break;
                        }

                        if (msg.startsWith(REG_OK.getCommand())) {
                            regController.appendToTextArea("Sign up is successful");
                        }
                        if (msg.startsWith(REG_NO.getCommand())) {
                            regController.appendToTextArea("Sign up failed: user already exists");
                        }

                        taChat.appendText(msg + endOfMsg);
                    }

                    while (true) {
                        String msg = dis.readUTF();

                        if (msg.startsWith("/")) {
                            if (msg.equals(END.getCommand())) {
                                break;
                            }
                            if (msg.startsWith(CLIENTS_LIST.getCommand())) {
                                String[] msgParts = msg.split("\\s+");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < msgParts.length; i++) {
                                        clientList.getItems().add(msgParts[i]);
                                    }
                                });
                            }
                        } else {
                            String message = formatMsg(msg);
                            taChat.appendText(message);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(nickname + " disconnected from server");
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setTitle(String nick) {
        Platform.runLater(() -> ((Stage)tfMessage.getScene().getWindow()).setTitle(TITLE +": "+ nick));
    }

    public void onActionBtnAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            dos.writeUTF(String.format("%s %s %s", AUTH.getCommand(),
                                                   tfLogin.getText().trim().toLowerCase(),
                                                   pfPassword.getText().trim()));
            tfLogin.clear();
            pfPassword.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onActionBtnReg(ActionEvent actionEvent) {
        regStage.show();
    }

    public void onActionBtnSend(ActionEvent actionEvent) {
        try {
//            String message = formatMsg(nickname, tfMessage.getText());
            dos.writeUTF(tfMessage.getText());
            tfMessage.clear();
            tfMessage.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onActionTfMessage(ActionEvent actionEvent) {
        onActionBtnSend(actionEvent);
    }

    public void onActionClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        tfMessage.setText("/w " + receiver + " ");
    }

    private String formatMsg(String msg) {
        if (msg.isBlank() || msg.equals(endOfMsg)) {
            return "";
        }

        return String.format("%s, %s%s",
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()),
                msg, endOfMsg);
    }

    public void register(String login, String pwd, String nick) {
        String msg = String.format("%s %s %s %s", REG.getCommand(), login, pwd, nick);

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);
    }

}
