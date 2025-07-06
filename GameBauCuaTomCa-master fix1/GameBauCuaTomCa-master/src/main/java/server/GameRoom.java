package server;

import com.google.gson.*;
import db.UserDAO;

import java.util.*;

public class GameRoom {
    private static final int MAX_PLAYER = 4;
    private final List<PlayerHandler> players = new ArrayList<>();
    private Map<PlayerHandler, int[]> bets = new HashMap<>();

    // 1. Thêm player vào phòng
   /*public synchronized void addPlayer(PlayerHandler p) {
        players.add(p);
        broadcastTableUpdate();
    } */

    public synchronized void addPlayer(PlayerHandler p) {
        removePlayerByUserId(p.getUserId()); // Xóa hết bản cũ trùng userId trước CON CAC
        players.add(p);
        System.out.println("[ADD] " + p.getPlayerName() + " - userId: " + p.getUserId() + " | Số người: " + players.size());
        broadcastTableUpdate();
    }



    // 2. Xóa player khỏi phòng
    // Trong GameRoom:
    public synchronized void removePlayerByUserId(int userId) {
        Iterator<PlayerHandler> it = players.iterator();
        while (it.hasNext()) {
            PlayerHandler p = it.next();
            if (p.getUserId() == userId) {
                it.remove();
                bets.remove(p);
            }
        }
        broadcastTableUpdate();
    }




    // 3. Kiểm tra đủ người chưa
    public synchronized boolean isFull() {
        return players.size() == MAX_PLAYER;
    }
    public synchronized boolean isEmpty() {
        return players.isEmpty();
    }

    // 4. Khi player đặt cược
    public synchronized void placeBet(PlayerHandler p, int[] bet) {
        bets.put(p, bet);
        if (bets.size() == players.size()) {
            runGameAndBroadcast();
            bets.clear();
        }
    }

    // 5. GỬI TABLE_UPDATE
    private void broadcastTableUpdate() {
        JsonObject msg = new JsonObject();
        msg.addProperty("action", "TABLE_UPDATE");

        JsonArray arr = new JsonArray();
        for (PlayerHandler p : players) {
            JsonObject pJson = new JsonObject();
            pJson.addProperty("name", p.getPlayerName());
            pJson.addProperty("avatar", p.getAvatarNum());
            pJson.addProperty("balance", p.getBalance());
            arr.add(pJson);
        }
        msg.add("players", arr);

        String jsonString = msg.toString();
        for (PlayerHandler p : players) {
            p.send(jsonString);
        }
    }

    // 6. GỬI DICE_RESULT
    private void runGameAndBroadcast() {
        Random rand = new Random();
        int[] dice = {rand.nextInt(6), rand.nextInt(6), rand.nextInt(6)};

        List<Double> balancesAfter = new ArrayList<>();


        for (PlayerHandler p : players) {
            int[] bet = bets.getOrDefault(p, new int[6]);
            double win = 0;

            int[] diceCount = new int[6];
            for (int d : dice) diceCount[d]++;



            for (int i = 0; i < 6; i++) {
                int count = 0;
                for (int d : dice) if (d == i) count++;
                if (bet[i] > 0 && count > 0) {
                    win += bet[i] * count; // lời
                    win += bet[i];
                }
            }


            p.addBalance(win);
            balancesAfter.add(p.getBalance());


            UserDAO.updateBalance(p.getUserId(), (int) p.getBalance());


        }

        JsonObject msg = new JsonObject();
        msg.addProperty("action", "DICE_RESULT");

        JsonArray diceArr = new JsonArray();
        for (int d : dice) diceArr.add(d);
        msg.add("dice", diceArr);

        JsonArray balancesArr = new JsonArray();
        for (double b : balancesAfter) balancesArr.add(b);
        msg.add("balances", balancesArr);

        msg.addProperty("resultText", makeResultText(dice));
        String jsonString = msg.toString();

        for (PlayerHandler p : players) {
            p.send(jsonString);
        }
    }

    // Trả chuỗi kết quả xúc xắc để client hiện lên
    private String makeResultText(int[] dice) {
        String[] symbolNames = {"Nai", "Bầu", "Gà", "Cá", "Cua", "Tôm"};
        return "Kết quả: " + symbolNames[dice[0]] + ", " + symbolNames[dice[1]] + ", " + symbolNames[dice[2]];
    }
}
