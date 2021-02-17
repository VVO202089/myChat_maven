package client;

import server.AuthMessage;
import server.Message;
import server.Registration_Authorization;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;

public class MyClient extends JFrame {

    private ServerService serverService;
    private final String TITLE_message = "Окно сообщения";
    private boolean userOnline;
    private boolean swapUsers;
    private String swapLogin;

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
                    if (userOnline) {
                        sendMessage(myMessage);
                        if (userOnline) {
                            new Thread(() -> {
                                while (true) {
                                    printToUI(mainChat, serverService.readMessages());
                                }
                            }).start();
                        }
                    }else {
                        JOptionPane.showMessageDialog(null, "Пользователь не авторизован");
                    }
                }
            }
        });

        add(mainChat);
        jPanel.add(send);
        jPanel.add(myMessage);
        add(jPanel);
    }

    private void sendMessage(JTextField myMessage) {

        serverService.sendMessage(myMessage.getText());
        myMessage.setText("");

    }

    private void initLoginPanel(JTextArea mainChat) {

        JPanel panel = new JPanel();
        JPanel panelLogin = new JPanel();
        JPanel panelButton = new JPanel();

        panelLogin.setLayout(new BoxLayout(panelLogin, BoxLayout.Y_AXIS));
        panelLogin.setSize(300, 50);
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.setSize(300, 50);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(300, 50);

        JTextField login = new JTextField();
        login.setToolTipText("Логин");
        JPasswordField password = new JPasswordField();
        password.setToolTipText("Пароль");
        JButton regButton = new JButton("Регистрация");
        JButton authButton = new JButton("Авторизоваться");
        JButton swapButton = new JButton("Сменить ник");
        JButton quitButton = new JButton("Выйти");
        JLabel authLabel = new JLabel("Offline");

        // регистрация
        regButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            String psw = new String(password.getPassword());

            if (lgn != null && psw != null && !lgn.isEmpty() && !psw.isEmpty()) {
                Registration_Authorization reg = new Registration_Authorization(lgn, psw);
                reg.registrationUsers();
                userOnline = reg.isRegistration();

                if (reg.getMessageUser() != "") {
                    JOptionPane.showMessageDialog(null, reg.getMessageUser());
                }
                // теперь мы должны отправить сообщение к серверу, чтобы он отправил всем клиентам
                try {
                    if (userOnline) {
                        serverService.authorization(lgn, psw);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                authLabel.setText(lgn.concat((reg.isRegistration()) ? " online" : " offline"));

            }
        });

        // авторизация
        authButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            String psw = new String(password.getPassword());
            if (lgn != null && psw != null && !lgn.isEmpty() && !psw.isEmpty()) {
                try {
                    Registration_Authorization authorization = new Registration_Authorization(lgn, psw);
                    authorization.authorizationUsers();
                    userOnline = authorization.isAutorization();
                    if (authorization.getMessageUser() != "") {
                        JOptionPane.showMessageDialog(null, authorization.getMessageUser());
                    }
                    // теперь мы должны отправить сообщение к серверу, чтобы он отправил всем клиентам
                    try {
                        if (userOnline) {
                            serverService.authorization(lgn, psw);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    authLabel.setText(lgn.concat((authorization.isAutorization()) ? " online" : " offline"));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                new Thread(() -> {
                    while (true) {
                        printToUI(mainChat, serverService.readMessages());
                    }
                }).start();
            }
        });

        // выйти из пользователя
        quitButton.addActionListener(actionEvent -> {
            serverService = new SocketServerService();
            serverService.closeConnection();
            login.setText("");
            password.setText("");
            authLabel.setText("offline");
        });

        // сменить ник
        swapButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            if (lgn != null && !lgn.isEmpty()) {
                String mess = "Введите новый логин";
                String newLogin = JOptionPane.showInputDialog(MyClient.this, mess);
                // Обработка введенного логина
                Registration_Authorization swap = new Registration_Authorization(newLogin, "");
                swap.swapUsers(login.getText());
                swapUsers = swap.isSwapUsers();
                if (swap.getMessageUser() != "") {
                    JOptionPane.showMessageDialog(null, swap.getMessageUser());
                    if (swapUsers) {
                        login.setText(swap.getLogin());
                        JOptionPane.getRootFrame().dispose();
                    }
                }
                authLabel.setText(login.getText().concat((swapUsers) ? " online" : " offline"));
            }
        });

        // добавление элементов на форму
        panelLogin.add(login);
        panelLogin.add(password);
        panelButton.add(regButton);
        panelButton.add(authButton);
        panelButton.add(swapButton);
        panelButton.add(quitButton);
        panel.add(panelLogin);
        panel.add(panelButton);
        add(panel);
        add(authLabel);
        /*add(login);
        add(password);
        add(regButton);
        add(authButton);
        add(swapButton);
        add(quitButton);
        add(authLabel);*/

    }

    private void printToUI(JTextArea mainChat, Message message) {
        mainChat.append("\n");
        mainChat.append((message.getNick() != null ? message.getNick() : "Сервер") + " написал: " + message.getMessage());
    }


}