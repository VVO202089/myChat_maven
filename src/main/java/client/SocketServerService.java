package client;

import com.google.gson.Gson;
import server.AuthMessage;
import server.Message;
import server.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketServerService implements ServerService {

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private boolean isConnected = false;

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void openConnection() {
        try {
            socket = new Socket("localhost", MyServer.PORT);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer,String> authorization(String login, String password) throws IOException {
        AuthMessage authMessage = new AuthMessage();
        authMessage.setLogin(login);
        authMessage.setPassword(password);
        dataOutputStream.writeUTF(new Gson().toJson(authMessage));

        authMessage = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);
        if (authMessage.isAuthenticated()) {
            isConnected = true;
        }

        Map<Integer,String> result = new HashMap<>();
        result.put(1,authMessage.getNick());
        result.put(2,authMessage.getMessageUser());

        return result;
    }

    @Override
    public void closeConnection() {
        try {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        Message msg = new Message();
        msg.setMessage(message);

        try {
            dataOutputStream.writeUTF(new Gson().toJson(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Message readMessages() {
        try {
            return new Gson().fromJson(dataInputStream.readUTF(), Message.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Message();
        }
    }
}