package server;

import java.sql.*;

public class Registration {

    private final String driverName = "com.mysql.jdbc.Driver";
    private final String connectionString = "jdbc:mysql://localhost:3306/users";
    private String login;
    private String password;

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;
    private static ResultSet resultInsert;

    public Registration(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void registrationUsers() {

        String querySelect = "SELECT login FROM table_users";
        // получаем драйвер
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            System.out.println("Не смог получить драйвер");
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

            if (resultSet.next()){
                System.out.println("Логин уже занят!");
                return;
            }else {
                // добавляем запись в таблицу table_users
                String queryInsert = "INSERT table_users(login,password) VALUES ('".concat(login).concat("','").concat(password).concat("');");
                resultInsert = statement.executeQuery(queryInsert);
                if (resultInsert.next()){
                    System.out.println("регистрация прошла успешно!");
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
            e.printStackTrace();
        }
    }
}
