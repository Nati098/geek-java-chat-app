package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.*;

import static data.Commands.*;
import static server.CustomServer.LOGFILE_PATH;

public class ClientHandler {
    private Logger logger;

    private CustomServer server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String id = "";
    private String savedLogin = "";

    public ClientHandler(CustomServer server, Socket socket) {
        setupLogger();

        try {
            this.server = server;
            this.socket = socket;

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(10000);
                    authentication();
                    readMessages();
                }
                catch (SocketTimeoutException ste) {
                    logger.warning("Session timeout");
//                    server.handleInactiveUser(this);
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }


            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getId() {
        return id;
    }

    private void setupLogger() {
        logger = Logger.getLogger(CustomServer.class.getName());
        logger.addHandler(new ConsoleHandler());
        try {
            Handler fileHandler = fileHandler = new FileHandler(LOGFILE_PATH,11 * 1024 * 1024, 11, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.setUseParentHandlers(false);
    }

    private void authentication() throws IOException {
        while (true) {
            String msg = dis.readUTF();
            if (msg.startsWith(AUTH.getCommand())) {
                String[] msgParts = msg.split("\\s"); // auth login pwd
                if (msgParts.length < 3) {
                    continue;
                }
                String savedNick = server.getAuthService().getNickByLoginPass(msgParts[1], msgParts[2]);
                String login = msgParts[1];
                if (savedNick != null) {
                    if (!server.isLoginAuthenticated(login)) {
                        socket.setSoTimeout(0);
                        logger.info("Session time is set to infinity");

                        id = savedNick;
                        savedLogin = login;
                        sendMessage(AUTHOK.getCommand() + " " + savedNick);

                        server.subscribe(this);
                        //server.broadcast(this, "is online");
                        logger.info(String.format("User %s connected", id));
                        break;
                    } else {
                        sendMessage("This user is already online");
                    }
                }
                else {
                    sendMessage("Wrong login/password");
                }
            }

            if (msg.startsWith(REG.getCommand())) {
                String[] msgParts = msg.split("\\s");
                if (msgParts.length < 4) {
                    continue;
                }

                if (server.getAuthService()
                        .registration(msgParts[1], msgParts[2], msgParts[3])) {
                    sendMessage(REG_OK.getCommand());
                } else {
                    sendMessage(REG_NO.getCommand());
                }
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String msg = dis.readUTF();

            if (msg.startsWith("/")) {
                if (msg.equals(END.getCommand())) {
                    dos.writeUTF(END.getCommand());
                    break;
                }

                if (msg.startsWith(PRIVATE_MSG.getCommand())) {
                    String[] token = msg.split("\\s+", 3);
                    if (token.length < 3) {
                        continue;
                    }
                    server.broadcast(this, token[1], token[2]);
                }

                if (msg.startsWith(CHANGE_NICK.getCommand())) {
                    String[] token = msg.split("\\s+", 2);
                    if (token.length < 2) {
                        continue;
                    }
                    if (server.getAuthService().changeNickByLogin(savedLogin, token[1]) != null) {
                        sendMessage(CHANGE_OK.getCommand());
                    }
                    else {
                        sendMessage(CHANGE_NO.getCommand());
                    }
                }
            }
            else {
                server.broadcast(this, msg);
            }
        }
    }

    private void closeConnection() {
        logger.warning(String.format("%s disconnected", id));
        server.unsubscribe(this);

//        server.broadcast(this, "went out from chat");

        try {
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
