
package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
            new Thread(() -> {
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
            }).start();

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
                // если команда "выйти из пользователя"
                System.out.println("Выход " + message.isQuitUser());
                if (message.isQuitUser()){
                    List<ClientHandler> clients = myServer.getClients();

                    // находим нужного клиента и его удаляем
                    for (ClientHandler handler: clients) {
                        if (handler.getNick() == nick){
                            clients.remove(handler);
                            System.out.println("Выход");
                        }
                    }
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message msg = new Message();
                    msg.setMessage(nick + " вышел из чата");
                    //myServer.broadcastMessage(msg);
                    myServer.subscribe(this);
                    return;
                }
                if (nick != null && !nick.isEmpty()) {
                    message.setAuthenticated(true);
                    message.setNick(nick);
                    this.nick = nick;
                    dataOutputStream.writeUTF(new Gson().toJson(message));
                    Message broadcastMsg = new Message();
                    broadcastMsg.setMessage(nick + " вошел в чат");
                    myServer.broadcastMessage(broadcastMsg);
                    myServer.subscribe(this);
                    this.nick = nick;
                    isAuthentification = true;
                    return;
                } else {
                    message.setAuthenticated(false);
                    isAuthentification = false;
                    return;
                }

            } catch (IOException e) {
                return;
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            Message message = new Gson().fromJson(dataInputStream.readUTF(), Message.class);
            message.setNick(nick);
            System.out.println(message);
            // отправка личного сообщения
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