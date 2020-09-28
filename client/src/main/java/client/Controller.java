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
import javafx.stage.WindowEvent;
import server.CustomServer;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ResourceBundle;

import static data.Commands.*;


public class Controller implements Initializable {
    private final String IP_ADDRESS = "localhost";
    private final String TITLE = "Chatty";
    private final String endOfMsg = "\n";
    private final String filenamePattern = "history/history_%s.txt";
    private final int HISTORY_LIMIT = 100; // how many strokes we should add in the beginning

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
            stage.setOnCloseRequest(event -> onCloseRequest(event));
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
            String history = readHistoryFromFile();
            if (history != null) {
                taChat.appendText("Restoring from file...\n");
                taChat.appendText(history);
            }
            else {
                taChat.appendText(formatMsg("Server:\nWelcome!\n"));
            }
        }

        setTitle(nickname);
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
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
                        String msg = dis. readUTF();
                        if (msg.startsWith(AUTHOK.getCommand())) {
                            nickname = msg.split(" ", 2)[1];  // /authok nick
                            setAuthenticated(true);
                            break;
                        }

                        if (msg.startsWith(REG_OK.getCommand())) {
                            taChat.appendText("Sign up is successful! Sign In, please\n");
                            Platform.runLater(() -> regStage.hide());
                        }
                        if (msg.startsWith(REG_NO.getCommand())) {
                            regController.appendToTextArea("Sign up failed: user already exists");
                        }
                        if (msg.startsWith(CHANGE_OK.getCommand())) {
                            taChat.appendText(formatMsg("Server:\nNick was changed!\n"));
                        }
                        if (msg.startsWith(CHANGE_NO.getCommand())) {
                            taChat.appendText(formatMsg("Server:\nNick wasn't changed!\n"));
                        }

                        taChat.appendText(msg + endOfMsg);
                    }

                    while (true) {
                        String msg = dis.readUTF();

                        if (msg.startsWith("/")) {
                            if (msg.equals(END.getCommand())) {
                                saveHistoryToFile();
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
        if (!receiver.equals(nickname)){
            tfMessage.setText("/w " + receiver + " ");
        }
        else {
            if (tfMessage.getText().startsWith(PRIVATE_MSG.getCommand())) {
                tfMessage.clear();
            }
        }
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

    private void onCloseRequest(WindowEvent event) {
        saveHistoryToFile();

        System.out.println("Goodbye!");
        if (socket != null && !socket.isClosed()) {
            try {
                dos.writeUTF("/end");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readHistoryFromFile() {
//        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format(filenamePattern, nickname)))) {
        try (RandomAccessFile raf = new RandomAccessFile(String.format(filenamePattern, nickname), "r")) {
            // go to the end of the file
            long currentPos = raf.length() - 2;

            // add messages from the end (100)
            int linesNumber = 0;
            StringBuilder msgs = new StringBuilder();
            for (long p = currentPos; p >=0; p--) {
                raf.seek(p);
                char c = (char) raf.readByte();
                msgs.append(c);

                if (c == '\n') {
                    linesNumber++;
                }

                if (linesNumber == HISTORY_LIMIT) {
                    break;
                }
            }

            return msgs.reverse().toString();

        } catch (IOException e1) {
            System.out.println("No such file or directory");
            return null;
        }
    }

    private void saveHistoryToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.format(filenamePattern, nickname), false))) {

            writer.write(taChat.getText());

            System.out.println("History was saved");
        } catch (IOException e) {
            System.out.println("Cannot save history to file");
        }

    }
}
