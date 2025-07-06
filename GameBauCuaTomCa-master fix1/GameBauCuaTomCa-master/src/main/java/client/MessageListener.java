package client;

public interface MessageListener {
    void onServerMessage(String message);
    void onDisconnect(Exception e);
}
