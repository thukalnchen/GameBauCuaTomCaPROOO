package server;

import com.google.gson.*;
import java.io.*;
import java.net.*;

public class PlayerHandler implements Runnable {
    private Socket socket;
    private GameRoom currentRoom;
    private PrintWriter out;
    private BufferedReader in;

    // Thêm thuộc tính thông tin người chơi:

    private int userId;

    private String playerName = "Guest";
    private int avatarNum = 0;
    private double balance = 10000;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public void setRoom(GameRoom room) {
        this.currentRoom = room;
    }
    // Getter cho thông tin người chơi (để TableUpdate lấy info):
    public int getUserId() { return userId; }
    public String getPlayerName() { return playerName; }
    public int getAvatarNum() { return avatarNum; }
    public double getBalance() { return balance; }
    public void addBalance(double amount) { this.balance += amount; }

    public void kickAndClose(String message) {
        try {
            if (message != null && out != null) {
                out.println(message);
            }
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Received from client: " + msg);

                // Parse JSON:
                JsonObject jsonMsg = JsonParser.parseString(msg).getAsJsonObject();
                String action = jsonMsg.get("action").getAsString();

                if (action.equals("JOIN_TABLE")) {
                    // Nhận thông tin user từ client (nếu có):
                    if (jsonMsg.has("id")) userId = jsonMsg.get("id").getAsInt();
                    if (jsonMsg.has("name")) playerName = jsonMsg.get("name").getAsString();
                    if (jsonMsg.has("avatar")) avatarNum = jsonMsg.get("avatar").getAsInt();
                    if (jsonMsg.has("balance")) balance = jsonMsg.get("balance").getAsDouble(); // VAILON SO DU 100000
                    // KHÔNG GÁN balance từ client, chỉ lấy từ DB hoặc giữ nguyên trên server!

                    GameRoom room = ServerMain.roomManager.joinAnyRoom(this);
                    setRoom(room);

                } else if (action.equals("PLACE_BET")) {
                    JsonArray betsArr = jsonMsg.getAsJsonArray("bets");
                    int[] bet = new int[6];
                    int totalBet = 0;
                    for (int i = 0; i < 6; i++) {
                        bet[i] = betsArr.get(i).getAsInt();
                        totalBet += bet[i];
                    }
                    if (totalBet > this.balance) {
                        // Gửi ERROR về client
                        JsonObject err = new JsonObject();
                        err.addProperty("action", "ERROR");
                        err.addProperty("message", "Không đủ tiền đặt cược!");
                        send(err.toString());
                        return;
                    } else {
                        this.balance -= totalBet;
                    }
                    if (currentRoom != null) currentRoom.placeBet(this, bet);

                } else if (action.equals("LEAVE_TABLE")) {
                    if (currentRoom != null) {
                        currentRoom.removePlayerByUserId(this.userId);
                        setRoom(null); // Dùng hàm setRoom
                        System.out.println("[INFO] Player " + this.userId + " Da roi khoi ban.");
                    }
                }else if (action.equals("ROLL_DICE")) {
                    // Chỉ dealer mới được phép lắc xúc xắc!
                    if (currentRoom != null && currentRoom.getDealer() == this) {
                        currentRoom.runGameAndBroadcast();
                        // Nếu muốn, xóa cược sau khi lắc
                        currentRoom.clearBets(); // Viết hàm này để reset bets nếu cần
                    }
                }






            }
        } catch (IOException | IllegalStateException e) {
            System.out.println("Client disconnected.");
        } finally {
            try { socket.close(); } catch (IOException ex) {}
            ServerMain.clients.remove(this);

            if (currentRoom != null) currentRoom.removePlayerByUserId(this.userId);
            currentRoom = null;
        }

    }
}
