package client;

import java.io.*;
import java.net.*;

public class GameClientConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;
    private MessageListener listener;

    public GameClientConnection(String host, int port, MessageListener listener) throws IOException {
        this.listener = listener;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        listenThread = new Thread(this::listenLoop);
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void send(String message) {
        out.println(message);
    }

    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                listener.onServerMessage(line);
            }
        } catch (IOException e) {
            listener.onDisconnect(e);
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
