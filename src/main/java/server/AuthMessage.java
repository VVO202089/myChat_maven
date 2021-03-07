package server;

public class AuthMessage {
    private String login;
    private String password;
    private String nick;
    private String messageUser;
    private boolean authenticated = false;
    private boolean isQuitUser;

    public boolean isQuitUser() {
        return isQuitUser;
    }

    public void setQuitUser(boolean quitUser) {
        isQuitUser = quitUser;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}