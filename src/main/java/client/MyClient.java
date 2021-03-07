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
    //private final String PATH = "D://JAVA/";
    private String loginPath = "";
    private String historyUser = "";
    private JTextArea textFieldHistory = new JTextArea();

    public MyClient() {
        super("Чат");
        serverService = new SocketServerService();
        serverService.openConnection();
        // общая панель
        JPanel jPanel = new JPanel();
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        //jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setSize(400, 300);
        // центральная чать окна
        JPanel panelmain = new JPanel();
        panelmain.setLayout(new BoxLayout(panelmain,BoxLayout.X_AXIS));
        panelmain.setSize(200,200);
        // панель с общим чатом
        JPanel mainChatPanel = new JPanel();
        mainChatPanel.setLayout(new BoxLayout(mainChatPanel,BoxLayout.Y_AXIS));
        mainChatPanel.setSize(200,200);
        // панель history
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel,BoxLayout.Y_AXIS));
        historyPanel.setSize(200,200);
        // панель myMessage
        JPanel myMessagePanel = new JPanel();
        myMessagePanel.setLayout(new BoxLayout(myMessagePanel,BoxLayout.X_AXIS));
        myMessagePanel.setSize(100,100);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(400, 400, 500, 300);

        JLabel lbMainChat = new JLabel("Общее окно сообщений");
        JTextArea mainChat = new JTextArea();
        mainChat.setSize(400, 400);

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

        // панель с чатом
        mainChatPanel.add(lbMainChat);
        mainChatPanel.add(mainChat);
        // панель с историей чата
        historyPanel.add(lbHistory);
        historyPanel.add(textFieldHistory);
        //добавление полей в центр окна
        panelmain.add(mainChatPanel);
        panelmain.add(historyPanel);
        // добавление в подвал
        myMessagePanel.add(myMessage);
        myMessagePanel.add(send);
        // добавление панелей на форму
        add(panelmain);
        add(myMessagePanel);
        //add(jPanel);
    }

    private void saveMessage(JTextField myMessage) {
        //String fullPATH = PATH.concat("history_").concat(loginPath).concat(".txt");
        String fullPATH = "history_".concat(loginPath).concat(".txt");
        serverService.saveMessage(myMessage.getText(),fullPATH);
        myMessage.setText("");
    }

    private void sendMessage(JTextField myMessage) {
        serverService.sendMessage(myMessage.getText());
    }

    private void loadHistory() {
        //String fullPATH = PATH.concat("history_").concat(loginPath).concat(".txt");
        String fullPATH = "history_".concat(loginPath).concat(".txt");
        // получаем только последние 100 строк из истории
        List<String> listHistory = serverService.loadHistory(fullPATH,100);
        StringBuilder sbHistory = new StringBuilder();

        Iterator iterator = listHistory.listIterator();

        while (iterator.hasNext()){
            sbHistory.append(iterator.next() + "\r\n");
        }

        String history = sbHistory.toString();
        history.replace("\r\n","<br/>");

        textFieldHistory.setText(history);
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
                        serverService.authorization(lgn, psw,false);
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
                            serverService.authorization(lgn, psw,false);
                            //loginPath = lgn;
                            // загрузим последние 100 строк истории чата
                            //loadHistory();
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

                if (userOnline){
                    loginPath = lgn;
                    // загрузим последние 100 строк истории чата
                    loadHistory();
                }
            }
        });

        // выйти из пользователя
        quitButton.addActionListener(actionEvent -> {
            String lgn = login.getText();
            String psw = new String(password.getPassword());
            if (lgn != null && psw != null && !lgn.isEmpty() && !psw.isEmpty()) {
                try {
                    serverService.authorization(lgn,psw,true);
                    login.setText("");
                    password.setText("");
                    authLabel.setText("offline");
                    loginPath = "";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    }

    private void printToUI(JTextArea mainChat, Message message) {
        mainChat.append("\n");
        mainChat.append((message.getNick() != null ? message.getNick() : "Сервер") + " написал: " + message.getMessage());
    }


}