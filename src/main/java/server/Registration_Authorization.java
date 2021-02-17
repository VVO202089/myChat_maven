package server;

import java.sql.*;

public class Registration_Authorization {

    private final String driverName = "com.mysql.jdbc.Driver";
    private final String connectionString = "jdbc:mysql://localhost:3306/users";
    private String login;
    private String newUser;
    private String password;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private int resultInsert;
    private int resultUpdate;
    private boolean registration;
    private boolean autorization = false;
    private boolean swapUsers = false;
    private String messageUser;

    public Registration_Authorization(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public boolean isAutorization() {
        return autorization;
    }

    public boolean isSwapUsers() {
        return swapUsers;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public boolean isRegistration() {
        return registration;
    }

    public void registrationUsers() {

        String querySelect = "SELECT login FROM table_users WHERE login = '".concat(login).concat("'");

        // получаем драйвер
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            messageUser = "Не смог получить драйвер";
            e.printStackTrace();
            return;
        }

        // устанавливаем соединение
        // не хорошо указывать логин и пароль в строке, но это учебная задача
        // проверяем, есть ли такой логин уже, или нет
        try {
            connection = DriverManager.getConnection(connectionString, "root", "Zorg123!!!");
            statement = connection.createStatement();
            resultSet = statement.executeQuery(querySelect);

            if (resultSet.next()) {
                messageUser = "Логин уже занят!";
                registration = false;
                return;
            } else {
                // добавляем запись в таблицу table_users
                String queryInsert = "INSERT table_users(login,password) VALUES ('".concat(login).concat("','").concat(password).concat("')");
                resultInsert = statement.executeUpdate(queryInsert);
                if (resultInsert > 0) {
                    messageUser = "Регистрация прошла успешно!";
                    registration = true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Соединение и БД не установлено!");
            e.printStackTrace();
            return;
        }
        // закрываем соединения
        try {
            connection.close();
        } catch (SQLException e) {
            messageUser = "Ошибка при закрытии соединения";
            e.printStackTrace();
        }
    }

    public void authorizationUsers() {
        // проверим, если пользователь в базе
        String query = "SELECT login FROM table_users WHERE login = '".concat(login).concat("' AND password = '").concat(password).concat("'");

        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            messageUser = "Не могу получить драйвер";
            e.printStackTrace();
            return;
        }

        try {
            connection = DriverManager.getConnection(connectionString, "root", "Zorg123!!!");
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                // так как в результате будет одна строка
                messageUser = "Авторизация прошла успешно!";
                autorization = true;
            } else {
                messageUser = "Авторизация не прошла!";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            messageUser = "Ошибка при закрытии соединения";
            e.printStackTrace();
            return;
        }
    }

    public void swapUsers(String prevLogin) {

        // проверим, если пользователь в базе
        String query = "SELECT login FROM table_users WHERE login = '".concat(login).concat("'");

        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            messageUser = "Не могу получить драйвер";
            e.printStackTrace();
            return;
        }

        try {
            connection = DriverManager.getConnection(connectionString, "root", "Zorg123!!!");
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                // Если пользователь уже есть в базе
                messageUser = "Этот логин уже занят, попробуйте другой!";
                swapUsers = false;
            } else {
                // меняем имя пользователя, делаем UPDATE
                String queryUpdate = "UPDATE table_users SET login = '".concat(login).concat("' WHERE login = '").concat(prevLogin).concat("'");
                resultUpdate = statement.executeUpdate(queryUpdate);

                if (resultUpdate > 0) {
                    messageUser = "Логин изменен!";
                    swapUsers = true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            messageUser = "Ошибка при закрытии соединения";
            e.printStackTrace();
            return;
        }

    }
}
