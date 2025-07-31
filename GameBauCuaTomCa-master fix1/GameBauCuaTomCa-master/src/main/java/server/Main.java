package server;

import client.NapTienScene;
import common.User;
import db.UserDAO;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Ví dụ login tạm: nhập từ console
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập username: ");
        String username = scanner.nextLine();
        System.out.print("Nhập password: ");
        String password = scanner.nextLine();

        User currentUser = UserDAO.login(username, password);
        if (currentUser != null) {
            System.out.println("Đăng nhập thành công!");

            WebServer.start(currentUser); // ✅ Gửi user vào server

        } else {
            System.out.println("❌ Đăng nhập thất bại. Thoát chương trình.");
        }
    }
}
