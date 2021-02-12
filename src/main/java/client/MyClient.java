package client;

import server.Message;
import server.Registration;

import javax.accessibility.AccessibleRole;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MyClient extends JFrame {

    private ServerService serverService;
    private final  String   TITLE_message = "Окно сообщения";

    public MyClient() {
        super("Чат");
        serverService = new SocketServerService();
        serverService.openConnection();
        JPanel jPanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setSize(300, 50);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(400, 400, 500, 300);

        JTextArea mainChat = new JTextArea();
        mainChat.setSize(400, 250);

        initLoginPanel(mainChat);

        JTextField myMessage = new JTextField();

        JButton send = new JButton("Send");
        send.addActionListener(actionEvent -> sendMessage(myMessage));

        myMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage(myMessage);
                }
            }
        });

        if (serverService.isConnected()) {
            new Thread(() -> {
                while (true) {
                    printToUI(mainChat, serverService.readMessages());
                }
            }).start();
        }

        add(mainChat);
        jPanel.add(send);
        jPanel.add(myMessage);
        add(jPanel);
    }

    private void initLoginPanel(JTextArea mainChat) {
        JTextField login = new JTextField();
        login.setToolTipText("Логин");
        JPasswordField password = new JPasswordField();
        password.setToolTipText("Пароль");
        JButton regButton = new JButton("Регистрация");
        JButton authButton = new JButton("Авторизоваться");

        // регистрация
        regButton.addActionListener(actionEvent ->{
            String lgn = login.getText();
            String psw = new String(password.getPassword());

            if(lgn!=null && psw!=null && !lgn.isEmpty() && !psw.isEmpty()){
                Registration reg = new Registration(lgn,psw);
                reg.registrationUsers();
            }

        });

        JLabel authLabel = new JLabel("Offline");
        // авторизация
        authButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            String psw = new String(password.getPassword());
            if (lgn != null && psw != null && !lgn.isEmpty() && !psw.isEmpty()) {
                try {
                    String nick = serverService.authorization(lgn, psw);
                    authLabel.setText("Online, nick "+nick);
                } catch (IOException e) {
                    e.printStackTrace();
                    // выход из приложения
                    //String errorText = "Время для авторизации истекло";
                    //int result = JOptionPane.showInternalConfirmDialog(MyClient.this,
                    //                                errorText, TITLE_message,
                    //                                JOptionPane.INFORMATION_MESSAGE);

                    //if (result == 1) {
                    //    System.exit(0);
                    //}
                    System.exit(0);
                    return;
                } finally {
                    if (!serverService.isConnected()){
                        serverService.closeConnection();
                        System.exit(0);
                        return;
                    }

                }
                new Thread(() -> {
                    while (true) {
                        printToUI(mainChat, serverService.readMessages());
                    }
                }).start();
            }
        });

        // добавление элементов на форму
        add(login);
        add(password);
        add(regButton);
        add(authButton);
        add(authLabel);

    }

    private void sendMessage(JTextField myMessage) {
        serverService.sendMessage(myMessage.getText());
        myMessage.setText("");
    }

    private void printToUI(JTextArea mainChat, Message message) {
        mainChat.append("\n");
        mainChat.append((message.getNick() != null ? message.getNick() : "Сервер") + " написал: " + message.getMessage());
    }


}