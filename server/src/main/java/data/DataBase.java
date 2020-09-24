package data;

import java.sql.*;

public class DataBase {

    public static String[] logins = {"4433", "3322", "4466"};
    public static String[] passwords = {"1", "1", "1"};
    public static String[] nicks = {"z", "x", "c"};

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedInsert;
    private static PreparedStatement preparedUpdate;
    private static PreparedStatement preparedSelect;

    // how we should to connect
    public void connect() throws ClassNotFoundException, SQLException {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            statement = connection.createStatement();  // to do requests to db

//            connection.setAutoCommit(false);
//            connection.commit();
    }

    public void disconnect() {
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

    private void insertUser(String login, String nickname, String password) throws SQLException {
        preparedInsert = connection.prepareStatement("INSERT INTO users (login, nickname, password) VALUES (?, ?, ?);");

        preparedInsert.setString(1, login);
        preparedInsert.setString(2, nickname);
        preparedInsert.setString(3, password);

        int updatedRows = preparedInsert.executeUpdate();
        System.out.println("Updates " + updatedRows + " rows");
    }

    // unsafe!!!
    private void insertUsers(String[] login, String[] nickname, String[] password) throws SQLException {
        if (login.length <= 0) {
            System.out.println("Empty array of users! Unable to add!");
            return;
        }

        for (int i = 1; i < login.length; i++) {
            preparedInsert.setString(1, login[i]);
            preparedInsert.setString(2, nickname[i]);
            preparedInsert.setString(3, password[i]);
            preparedInsert.addBatch();
        }
        int[] updatedRows = preparedInsert.executeBatch();

        System.out.print("Updated rows: ");
        for (int row : updatedRows) {
            System.out.print(row+" ");
        }
    }

    private void updateUserNickname(String login, String newNickname) throws SQLException {
        preparedUpdate = connection.prepareStatement("UPDATE users SET nickname=? WHERE login=?");

        preparedInsert.setString(1, newNickname);
        preparedInsert.setString(2, login);

        int updatedRows = preparedUpdate.executeUpdate();
        System.out.println("Updates " + updatedRows + " rows");
    }

    private void clearDB() throws SQLException {
        statement.executeUpdate("DELETE FROM users");
    }

    private void selectUsers(String login) throws SQLException {
        preparedSelect = connection.prepareStatement("SELECT * from users WHERE login=?");

        preparedSelect.setString(1, login);

        ResultSet rs = preparedSelect.executeQuery();
        while (rs.next()) {
            // must be UserData object
            String log = rs.getString("login");
            String nick = rs.getString("nickname");
            String pass = rs.getString("password");
        }
        rs.close();
    }

    public void main(String[] args) {
        try {
            connect();
            System.out.println("Connected!");

            // here different changes with bd

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

}
