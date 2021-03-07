package client;

import server.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ServerService {

    boolean isConnected();
    void openConnection();
    void closeConnection();
    void authorization(String login, String password,boolean quit) throws IOException;
    void sendMessage(String message);
    void saveMessage(String message,String fullPATH);
    List<String> loadHistory(String fullPATH, int nLines);
    Message readMessages();

}