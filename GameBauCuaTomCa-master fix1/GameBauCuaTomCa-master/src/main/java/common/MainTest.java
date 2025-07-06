package common;

import db.UserDAO;


public class MainTest {
    public static void main(String[] args) {
        // Đăng ký
        User user = new User(0, "tester", "123456", "Nguyễn Văn Tester", 1, 100000);
        boolean ok = UserDAO.register(user);
        System.out.println("Đăng ký thành công: " + ok);

        // Đăng nhập
        User loginUser = UserDAO.login("tester", "123456");
        if (loginUser != null) {
            System.out.println("Đăng nhập thành công! Xin chào " + loginUser.getName());
        } else {
            System.out.println("Đăng nhập thất bại!");
        }
    }
}

// VY NGUUU NHU BO