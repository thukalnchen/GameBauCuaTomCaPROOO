package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import payment.PayPalService;
import common.User;
import db.UserDAO; // giả sử class này chứa updateBalance(...)


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class WebServer {
    public static boolean paymentDone = false;  // Cai nay de check xem giao dịch đã hoàn thành hay chưa gehehhe
    private static User loggedInUser;

    public static User getLoggedInUser() {
        return loggedInUser;
    }


    public static void start(User currentUser) throws IOException {


        loggedInUser = currentUser; // Gán user hiện tại

        HttpServer server = HttpServer.create(new InetSocketAddress(4567), 0);

        server.createContext("/success", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String paymentId = params.get("paymentId");
            String payerId = params.get("PayerID");
            String amountStr = params.get("amount"); // ✅ Lấy số tiền từ URL

            String resultMessage;

            if (paymentId != null && payerId != null && amountStr != null) {
                PayPalService payPalService = new PayPalService();
                try {
                    payPalService.executePayment(paymentId, payerId); // xác nhận

                    int amountUSD = Integer.parseInt(amountStr); // ✅ Convert sang int
                    int vndAmount = amountUSD * 24000; // tỷ giá
                    int newBalance = loggedInUser.getBalance() + vndAmount;

                    boolean updated = UserDAO.updateBalance(loggedInUser.getId(), newBalance);

                    if (updated) {
                        loggedInUser.setBalance(newBalance);
                        boolean logged = UserDAO.logNapTien(
                                loggedInUser.getId(),
                                vndAmount,
                                paymentId,
                                payerId,
                                new Timestamp(System.currentTimeMillis())
                        );

                        resultMessage = "<h1>Thanh toán thành công! Đã cộng " + vndAmount + " VNĐ vào tài khoản.</h1>";
                        WebServer.paymentDone = true; //   KIEM TRA XEM GIAO DICH DA HOAN THANH HAY CHUA NUA NE +)))

                    } else {
                        resultMessage = "<h1>Thanh toán xong nhưng lỗi cập nhật số dư!</h1>";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    resultMessage = "<h1>Lỗi khi xác nhận thanh toán!</h1>";
                }
            } else {
                resultMessage = "<h1>Không tìm thấy thông tin giao dịch!</h1>";
            }

            sendResponse(exchange, resultMessage);
        });


        server.createContext("/cancel", exchange -> {
            String response = "<h1>Giao dịch đã bị hủy.</h1>";
            sendResponse(exchange, response);
        });

        server.setExecutor(null);
        server.start();
        System.out.println("✅ Callback Server đang chạy tại http://localhost:4567/");
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.flush();
        os.close();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }
}
