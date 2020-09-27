package data;

import java.sql.*;

public class DBHandler {

    public static String[] logins = {"4433", "3322", "4466"};
    public static String[] passwords = {"1", "1", "1"};
    public static String[] nicks = {"z", "x", "c"};

    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement preparedRegister;
    private static PreparedStatement preparedGetNick;
    private static PreparedStatement preparedChangeNick;


    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            statement = connection.createStatement();  // to do requests to db

            createPreparedStatements();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createPreparedStatements() throws SQLException {
        preparedRegister = connection.prepareStatement("INSERT INTO users (login, nickname, password) VALUES (?, ?, ?);");
        preparedGetNick = connection.prepareStatement("SELECT nickname from users WHERE login=?");
        preparedChangeNick = connection.prepareStatement("UPDATE users SET nickname=? WHERE login=?");
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            preparedRegister.setString(1, login);
            preparedRegister.setString(2, nickname);
            preparedRegister.setString(3, password);

            int updatedRows = preparedRegister.executeUpdate();
            System.out.println("Updates " + updatedRows + " rows");

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getNickByLoginPass(String login) {
        String res = null;
        try {
            preparedGetNick.setString(1, login);
            ResultSet rs = preparedGetNick.executeQuery();
            while (rs.next()) {
                res = rs.getString("nickname");
            }
            rs.close();

            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    public static String changeNickByLogin(String login, String newNick) {
        try {
            preparedChangeNick.setString(1, newNick);
            preparedChangeNick.setString(2, login);

            int updatedRows = preparedChangeNick.executeUpdate();
            System.out.println("Updates " + updatedRows + " rows");
            return newNick;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }

}
