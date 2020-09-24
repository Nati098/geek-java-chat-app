package server;

public interface AuthService {
    void start();
    String getNickByLoginPass(String login, String pwd);  // если пользователь есть - nickname, иначе - null
    boolean registration(String login, String password, String nickname);
    void stop();
}
