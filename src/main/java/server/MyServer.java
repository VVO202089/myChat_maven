package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyServer {
    public static final int PORT = 8082;

    private List<ClientHandler> clients;
    private AuthService authService;
    private String nick;
    protected static final Logger LOGGER = Logger.getLogger("");

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //authService = new BaseAuthService();
            //authService.start();
            clients = new ArrayList<>();
            while (true) {
                //System.out.println("Ожидаем поключение клиентов");
                LOGGER.log(Level.INFO,"Ожидаем поключение клиентов");
                Socket socket = serverSocket.accept();
                //System.out.println("Клиент подключился");
                LOGGER.log(Level.INFO,"Клиент подключился");
                new ClientHandler(this, socket,nick);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getNick()).append(" ");
        }
        Message message = new Message();
        message.setMessage(sb.toString());
        broadcastMessage(message);
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nickTo)) {
                //System.out.printf("Отправляем личное сообщение от %s, кому %s", from.getNick(), nickTo);
                LOGGER.log(Level.INFO,String.format("Отправляем личное сообщение от %s, кому %s",from.getNick(), nickTo));
                Message message = new Message();
                message.setNick(from.getNick());
                message.setMessage(msg);
                client.sendMessage(message);
                return;
            }
        }
        //System.out.printf("Клиент с ником %s не подюклчен к чату", nickTo);
        LOGGER.log(Level.INFO,String.format("Клиент с ником %s не подключен к чату",nickTo));
        Message message = new Message();
        //message.setMessage("Клиент с этим ником не подключен к чату");
        LOGGER.log(Level.INFO,"Клиент с ником %s не подключен к чату");
        from.sendMessage(message);
    }

    public synchronized void broadcastMessage(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        /*for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }*/

        return false;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }
    public synchronized void delClients(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }
}