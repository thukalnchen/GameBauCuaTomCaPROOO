package server;

import com.google.gson.*;
import db.UserDAO;

import java.util.*;

public class GameRoom {
    private PlayerHandler dealer;
    private static final int MAX_PLAYER = 4;
    private final List<PlayerHandler> players = new ArrayList<>();
    private Map<PlayerHandler, int[]> bets = new HashMap<>();

    // 1. Thêm player vào phòng
   /*public synchronized void addPlayer(PlayerHandler p) {
        players.add(p);
        broadcastTableUpdate();
    } */

    public synchronized void addPlayer(PlayerHandler p) {
        removePlayerByUserId(p.getUserId()); // Xoá player cũ nếu trùng userId
        players.add(p);

        // Nếu chưa có dealer, người mới vào sẽ làm dealer
        if (dealer == null) {
            dealer = p;
        }

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
                // Nếu dealer rời bàn
                if (dealer == p) {
                    it.remove();
                    bets.remove(p);
                    // Gán dealer mới: người còn lại đầu tiên (nếu còn), hoặc null nếu bàn trống
                    dealer = players.isEmpty() ? null : players.get(0);
                    broadcastTableUpdate();
                    return;
                } else {
                    it.remove();
                    bets.remove(p);
                    broadcastTableUpdate();
                    return;
                }
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
            pJson.addProperty("userId", p.getUserId());
            pJson.addProperty("name", p.getPlayerName());
            pJson.addProperty("avatar", p.getAvatarNum());
            pJson.addProperty("balance", p.getBalance());
            arr.add(pJson);
        }
        msg.add("players", arr);

        // Thêm dealer info
        if (dealer != null) {
            msg.addProperty("dealerId", dealer.getUserId());
            msg.addProperty("dealerName", dealer.getPlayerName());
        } else {
            msg.addProperty("dealerId", -1);
            msg.addProperty("dealerName", "");
        }

        String jsonString = msg.toString();
        for (PlayerHandler p : players) {
            p.send(jsonString);
        }
    }


    // 6. GỬI DICE_RESULT  ( KET QUA XUC SAC)
    public void runGameAndBroadcast() {
        Random rand = new Random();
        int[] dice = {rand.nextInt(6), rand.nextInt(6), rand.nextInt(6)};
        int[] diceCount = new int[6];
        for (int d : dice) diceCount[d]++;

        PlayerHandler dealer = this.dealer;
        double dealerDelta = 0;

        List<Double> balancesAfter = new ArrayList<>();
        balancesAfter.add(dealer.getBalance()); // placeholder


        List<PlayerHandler> kickedPlayers = new ArrayList<>();         // Danh sách lưu người chơi cần kick (không phải dealer)

        for (PlayerHandler p : players) {
            if (p == dealer) continue;

            int[] bet = bets.getOrDefault(p, new int[6]);
            double playerDelta = 0;

            for (int i = 0; i < 6; i++) {   // Xử Lí logic thắng thua tiền cược ở đây nè :VVV
                if (bet[i] > 0) {
                    int count = diceCount[i];
                    if (count > 0) {
                        // Player thắng: dealer phải trả
                        double dealerBalance = dealer.getBalance();
                        double win = bet[i] * count + bet[i];
                        double lose_nhacai = bet[i] * count;

                        if (dealerBalance >= lose_nhacai) {
                            playerDelta += win;
                            dealerDelta -= lose_nhacai;
                            dealer.addBalance(-lose_nhacai);
                        } else if (dealerBalance > 0) {
                            playerDelta += dealerBalance + bet[i]; // Trả lại tiền cược + số tiền dealer có thể trả
                            dealerDelta -= dealerBalance;
                            dealer.addBalance(-dealerBalance); // dealer về 0
                        }
                    } else {
                        dealerDelta += bet[i];
                        dealer.addBalance(bet[i]);
                    }
                }
            }

            p.addBalance(playerDelta);
            if (p.getBalance() < 0) p.addBalance(-p.getBalance());
            UserDAO.updateBalance(p.getUserId(), (int) p.getBalance());
            balancesAfter.add(p.getBalance());

            // **Kick player nếu balance < 200**
            if (p.getBalance() < 200) {
                kickedPlayers.add(p);
            }
        }

        // Đảm bảo dealer không âm tiền
        if (dealer.getBalance() < 0) dealer.addBalance(-dealer.getBalance());
        UserDAO.updateBalance(dealer.getUserId(), (int) dealer.getBalance());
        balancesAfter.set(0, dealer.getBalance());

        // Gửi kết quả trước
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

        // 1. Gửi thông báo cho người chơi còn dưới 200 và đặt hẹn giờ để kick
        for (PlayerHandler p : kickedPlayers) {
            // Tạo một thread riêng để delay việc kick người chơi
            new Thread(() -> {
                try {
                    // Delay 5 giây để người chơi có thể xem kết quả
                    Thread.sleep(5000);
                    
                    // Kiểm tra xem người chơi còn kết nối không trước khi kick
                    if (players.contains(p)) {
                        JsonObject kickMsg = new JsonObject();
                        kickMsg.addProperty("action", "KICKED");
                        kickMsg.addProperty("message", "Bạn đã bị đá khỏi bàn do số dư dưới 200!");
                        kickMsg.addProperty("balance", p.getBalance());
                        p.send(kickMsg.toString());
                        
                        // Delay thêm 1 giây để người chơi nhìn thấy thông báo
                        Thread.sleep(1000);
                        
                        p.kickAndClose(null);
                        removePlayerByUserId(p.getUserId());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        
        // 2. Nếu dealer < 200 thì gửi thông báo và đặt hẹn giờ để kick dealer
        boolean dealerKicked = false;
        if (dealer.getBalance() < 200) {
            dealerKicked = true;
            
            // Tạo bản sao final của dealer hiện tại
            final PlayerHandler dealerToKick = dealer;
            
            // Tạo một thread riêng để delay việc kick dealer
            new Thread(() -> {
                try {
                    // Delay 5 giây để dealer có thể xem kết quả
                    Thread.sleep(5000);
                    
                    // Kiểm tra xem dealer còn kết nối không trước khi kick
                    if (players.contains(dealerToKick)) {
                        JsonObject kickMsg = new JsonObject();
                        kickMsg.addProperty("action", "KICKED");
                        kickMsg.addProperty("message", "Bạn đã bị đá khỏi bàn do làm cái mà số dư dưới 200!");
                        kickMsg.addProperty("balance", dealerToKick.getBalance());
                        dealerToKick.send(kickMsg.toString());
                        
                        // Delay thêm 1 giây để dealer nhìn thấy thông báo
                        Thread.sleep(1000);
                        
                        dealerToKick.kickAndClose(null);
                        removePlayerByUserId(dealerToKick.getUserId());
                        
                        // Chọn dealer mới nếu còn player khác
                        synchronized (GameRoom.this) {
                            if (!players.isEmpty()) {
                                GameRoom.this.dealer = players.get(0);
                                broadcastTableUpdate();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }


        // Không xử lý chọn dealer mới ở đây vì đã được xử lý trong thread delay
        // Broadcast table update để cập nhật UI cho tất cả player còn lại
        broadcastTableUpdate();
        
    }







    // Trả chuỗi kết quả xúc xắc để client hiện lên
    private String makeResultText(int[] dice) {
        String[] symbolNames = {"Nai", "Bầu", "Gà", "Cá", "Cua", "Tôm"};
        return "Kết quả: " + symbolNames[dice[0]] + ", " + symbolNames[dice[1]] + ", " + symbolNames[dice[2]];
    }

    public synchronized void clearBets() {
        bets.clear();
    }

    public PlayerHandler getDealer() {
        return dealer;
    }

    public synchronized boolean isPlayerInRoom(PlayerHandler player) {
        return players.contains(player);
    }

}
