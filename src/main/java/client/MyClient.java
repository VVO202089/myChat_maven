package client;

import server.AuthMessage;
import server.Message;
import server.Registration_Authorization;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyClient extends JFrame {

    private ServerService serverService;
    private final String TITLE_message = "Окно сообщения";
    private boolean userOnline;
    private boolean swapUsers;
    private String swapLogin;
    private final String PATH = "D://JAVA база/";
    private String loginPath = "";
    private String historyUser = "";
    private JTextField textFieldHistory = new JTextField();

    public MyClient() {
        super("Чат");
        serverService = new SocketServerService();
        serverService.openConnection();
        // общая панель
        JPanel jPanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setSize(300, 50);
        // панель myMessage
        JPanel myMessagePanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        myMessagePanel.setSize(100,70);
        // панель history
        JPanel historyPanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        myMessagePanel.setSize(70,70);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(400, 400, 500, 300);

        JLabel lbMainChat = new JLabel("Общее окно");
        JTextArea mainChat = new JTextArea();
        mainChat.setSize(400, 250);

        initLoginPanel(mainChat);

        JLabel lbMymessage = new JLabel("Введите сообщение");
        JLabel lbHistory = new JLabel("История сообщений");
        JTextField myMessage = new JTextField();
        JButton send = new JButton("Отправить сообщение");

        send.addActionListener(actionEvent -> sendMessage(myMessage));

        myMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {

                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (userOnline) {
                        // отправка сообщения
                        sendMessage(myMessage);
                        // логирование сообщений
                        saveMessage(myMessage);
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

        add(lbMainChat);
        add(mainChat);
        myMessagePanel.add(lbMymessage);
        myMessagePanel.add(myMessage);
        historyPanel.add(lbHistory);
        historyPanel.add(textFieldHistory);
        jPanel.add(send);
        jPanel.add(myMessagePanel);
        jPanel.add(historyPanel);
        add(jPanel);
    }

    private void saveMessage(JTextField myMessage) {
        String fullPATH = PATH.concat("history_").concat(loginPath).concat(".txt");
        serverService.saveMessage(myMessage.getText(),fullPATH);
    }

    private void sendMessage(JTextField myMessage) {

        serverService.sendMessage(myMessage.getText());
        myMessage.setText("");

    }

    private void loadHistory() {
        String fullPATH = PATH.concat("history_").concat(loginPath).concat(".txt");
        // получаем только последние 100 строк из истории
        List<String> listHistory = serverService.loadHistory(fullPATH,100);
        StringBuilder sbHistory = new StringBuilder();

        Iterator iterator = listHistory.listIterator();

        while (iterator.hasNext()){
            sbHistory.append(iterator.next());
        }

        textFieldHistory.setText(sbHistory.toString());
    }

    private void initLoginPanel(JTextArea mainChat) {

        JPanel panel = new JPanel();
        JPanel panelLogin = new JPanel();
        JPanel panelButton = new JPanel();

        panelLogin.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        panelLogin.setSize(300, 50);
        panelButton.setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        panelButton.setSize(300, 50);
        panel.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
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
                loginPath = lgn;

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
                loginPath = lgn;
                // загрузим последние 100 строк истории чата
                loadHistory();
            }
        });

        // выйти из пользователя
        quitButton.addActionListener(actionEvent -> {
            serverService = new SocketServerService();
            serverService.closeConnection();
            login.setText("");
            password.setText("");
            authLabel.setText("offline");
            loginPath = "";
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
                loginPath = newLogin;
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