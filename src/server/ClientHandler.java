package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static data.Commands.*;

public class ClientHandler {
    private CustomServer server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String id = "";

    public ClientHandler(CustomServer server, Socket socket) {
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
                    System.out.println("Session timeout");
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
                        System.out.println("Session time is set to infinity");

                        id = savedNick;
                        sendMessage(AUTHOK.getCommand() + " " + savedNick);

                        server.subscribe(this);
                        //server.broadcast(this, "is online");
                        System.out.println(String.format("User %s connected", id));
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
            }
            else {
                server.broadcast(this, msg);
            }
        }
    }

    private void closeConnection() {
        System.out.println(String.format("%s disconnected", id));
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
