package server;

import data.DBHandler;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private List<UserData> users;

    public BaseAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < DBHandler.logins.length; i++) {
            users.add(new UserData(DBHandler.logins[i],
                                   DBHandler.passwords[i],
                                   DBHandler.nicks[i]));
        }
    }

    @Override
    public void start() {
        System.out.println("Auth Service started");
    }

    @Override
    public String getNickByLoginPass(String login, String pwd) {
        for (UserData u : users) {
            if (u.login.equals(login) && u.password.equals(pwd)) {
                return u.nickname;
            }
        }

        return null;
    }

    @Override
    public String changeNickByLogin(String login, String newNick) {
        return null;
    }

    @Override
    public boolean registration(String login, String pwd, String nick) {
        for (UserData u : users) {
            if (u.login.equals(login) || u.nickname.equals(nick)) {
                return false;
            }
        }

        users.add(new UserData(login, pwd, nick));
        return true;
    }

    @Override
    public void stop() {
        System.out.println("Auth Service stopped");
    }


    private class UserData {
        private String login;
        private String password;
        private String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }

    }
}
