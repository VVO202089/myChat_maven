package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nick;
    private boolean isAuthentification = false;

    public ClientHandler(MyServer myServer, Socket socket, String nick) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.nick = nick;
            // комментируем
            // д.з 8 урок
            /*new Thread(() ->{
                try {
                    Thread.sleep(120000); // усыпляем на 120 сек
                    // если авторизация не прошла, тогда закрываем соединение
                    if (!isAuthentification){
                        closeConnection();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                    return;
                }
            }).start();*/

            //new Thread(() -> {
                try {
                    // авторизация происходит через БД
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    closeConnection();
                }
           // }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        Message message = new Message();
        message.setMessage(nick + " вышел из чата");
        myServer.broadcastMessage(message);
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authentication() {
        while (true) {
            try {
                AuthMessage message = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);
                String nick = message.getLogin();
                //String nick = myServer.getAuthService().getNickByLoginAndPass(message.getLogin(), message.getPassword());
                //if (nick != null && !myServer.isNickBusy(nick)) {
                if (nick != null && !nick.isEmpty()) {
                    message.setAuthenticated(true);
                    message.setNick(nick);
                    //message.setMessageUser("Авторизация прошла успешно");
                    this.nick = nick;
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message broadcastMsg = new Message();
                    broadcastMsg.setMessage(nick + " вошел в чат");
                    myServer.broadcastMessage(broadcastMsg);
                    myServer.subscribe(this);
                    this.nick = nick;
                    isAuthentification = true;
                    return;
                }else{
                    //message.setMessageUser("Ошибка при авторизации");
                    message.setAuthenticated(false);
                    isAuthentification = false;
                    return;
                }

            } catch (IOException ignored) {
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            Message message = new Gson().fromJson(dataInputStream.readUTF(), Message.class);
            message.setNick(nick);
            System.out.println(message);
            if (!message.getMessage().startsWith("/")) {
                myServer.broadcastMessage(message);
                continue;
            }
            // /<command> <message>
            String[] tokens = message.getMessage().split("\\s");
            switch (tokens[0]) {
                case "/end": {
                    return;
                }
                case "/w": {// /w <nick> <message>
                    if (tokens.length < 3) {
                        Message msg = new Message();
                        msg.setMessage("Не хватает параметров, необходимо отправить команду следующего вида: /w <ник> <сообщение>");
                        this.sendMessage(msg);
                    }
                    String nick = tokens[1];
                    String msg = tokens[2];
                    myServer.sendMsgToClient(this, nick, msg);
                    break;
                }
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            dataOutputStream.writeUTF(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}