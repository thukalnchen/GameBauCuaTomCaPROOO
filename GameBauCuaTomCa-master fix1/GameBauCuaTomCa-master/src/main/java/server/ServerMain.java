package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMain {
    public static RoomManager roomManager = new RoomManager();

    public static List<PlayerHandler> clients = new CopyOnWriteArrayList<>();     // Ds cac client dang ket noi

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Server started on port 8888");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            PlayerHandler handler = new PlayerHandler(clientSocket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }
}
