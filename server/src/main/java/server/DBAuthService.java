package server;

import data.DBHandler;

public class DBAuthService implements AuthService{
    @Override
    public void start() {
        System.out.println("Auth Service started");
    }

    @Override
    public String getNickByLoginPass(String login, String pwd) {
        return DBHandler.getNickByLoginPass(login);
    }

    @Override
    public String changeNickByLogin(String login, String newNick) {
        return DBHandler.changeNickByLogin(login, newNick);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return DBHandler.registration(login, password, nickname);
    }

    @Override
    public void stop() {
        System.out.println("Auth Service stopped");
    }
}
