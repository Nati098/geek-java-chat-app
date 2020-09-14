package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static data.Commands.CLIENTS_LIST;

public class CustomServer {
    public static final int PORT = 8189;
    private ServerSocket server;
    private Socket socket;

    private List<ClientHandler> clients;
    private AuthService authService;

    public CustomServer() {
        clients = new ArrayList<>();
        authService = new BaseAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server is running");

            authService.start();

            while (true) {
                socket = server.accept();  // переводим основной поток в режим ожидания
                System.out.println("Client is authorized");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            System.out.println("Error while server is working");
        } finally {
            if (authService != null) {
                authService.stop();
            }

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(ClientHandler c) {
        clients.add(c);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler c) {
        clients.remove(c);
        broadcastClientList();
    }

    public void broadcast(ClientHandler sender, String msg) {
        String message = String.format("%s:\n%s\n", sender.getId(), msg);
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public void broadcast(ClientHandler sender, String receiver, String msg) {
        String message = String.format("%s [private] :\n%s", sender.getId(), msg);
        for (ClientHandler c : clients) {
            if (c.getId().equals(receiver)) {
                c.sendMessage(message);
                if (!c.equals(sender)) {
                    sender.sendMessage(message);
                }
                return;
            }
        }

        sender.sendMessage(String.format("User %s not found", receiver));
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getId().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder(CLIENTS_LIST.getCommand());
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getId()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    public void handleInactiveUser(ClientHandler user) {

    }

    public AuthService getAuthService() {
        return authService;
    }

}
