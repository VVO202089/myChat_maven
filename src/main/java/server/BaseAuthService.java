package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private final String driverName = "com.mysql.jdbc.Driver";
    private final String connectionString = "jdbc:mysql://localhost:3306/users";

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private boolean authentification;
    private String messageUser;

    public String getMessageUser() {
        return messageUser;
    }

    //private List<Entry> entries;

  /*  public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("ivan", "1", "Neivanov"));
        entries.add(new Entry("sharik", "2", "Auf"));
        entries.add(new Entry("otvertka", "3", "Kruchu-verchu"));
    }*/

    /*private class Entry {
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }*/

    @Override
    public void start() {
        System.out.println("Сервис авторизации запущен");
    }

    @Override
    public void stop() {
        System.out.println("Сервис авторизации остановлен");
    }

    @Override
    public String getNickByLoginAndPass(String login, String password) {
        /*for (Entry entry : entries) {
            if (login.equals(entry.login) && password.equals(entry.password)) {
                return entry.nick;
            }
        } */
        // проверим, если пользователь в базе
        String query = "SELECT login FROM table_users WHERE login = '".concat(login).concat("' AND password = '").concat(password).concat("'");

        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            messageUser = "Не могу получить драйвер";
            e.printStackTrace();
            return null;
        }

        try {
            connection = DriverManager.getConnection(connectionString,"root","Zorg123!!!");
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet.next()){
                // так как в результате будет одна строка
                return resultSet.getString("login");
            }else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            messageUser = "Ошибка при закрытии соединения";
            e.printStackTrace();
            return null;
        }
        return null;
    }
}