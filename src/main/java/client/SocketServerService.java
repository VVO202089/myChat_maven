package client;

import com.google.gson.Gson;
import server.AuthMessage;
import server.Message;
import server.MyServer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void authorization(String login, String password) throws IOException {
        AuthMessage authMessage = new AuthMessage();
        authMessage.setLogin(login);
        authMessage.setPassword(password);
        dataOutputStream.writeUTF(new Gson().toJson(authMessage));

        authMessage = new Gson().fromJson(dataInputStream.readUTF(), AuthMessage.class);
        /*if (authMessage.isAuthenticated()) {
            isConnected = true;
        }

        Map<Integer,String> result = new HashMap<>();
        result.put(1,authMessage.getNick());
        result.put(2,authMessage.getMessageUser());*/
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
    public Message readMessages() {
        try {
            return new Gson().fromJson(dataInputStream.readUTF(), Message.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Message();
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
    public void saveMessage(String message, String fullPATH) {

        try {

            File history = new File(fullPATH);
            if (!history.exists()){
                history.createNewFile();
            }

            PrintWriter fileWriter = new PrintWriter(new FileWriter(history,true));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write((message.concat("\n")));
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> loadHistory(String fullPATH, int nLines) {

        File history = new File(fullPATH);

        try (Stream<String> stream = Files.lines(history.toPath())){
            return stream.limit(nLines).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

}