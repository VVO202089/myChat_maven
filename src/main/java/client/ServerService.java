package client;

import server.Message;

import java.io.IOException;
import java.util.Map;

public interface ServerService {

    boolean isConnected();
    void openConnection();
    void closeConnection();
    void authorization(String login, String password) throws IOException;

    void sendMessage(String message);
    Message readMessages();

}