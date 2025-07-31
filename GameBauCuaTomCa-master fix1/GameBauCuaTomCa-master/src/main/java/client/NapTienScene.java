package client;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import payment.PayPalService;
import server.WebServer;

import java.awt.Desktop;
import java.net.URI;
public class NapTienScene extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Nạp tiền qua PayPal");

        // Ảnh biểu tượng PayPal
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/paypal.png")));
        logo.setFitHeight(80);
        logo.setFitWidth(80);
        logo.setPreserveRatio(true);

        Label title = new Label("NẠP TIỀN");
        title.setFont(new Font("Arial", 24));
        title.setTextFill(Color.web("#003087")); // màu PayPal

        VBox header = new VBox(10, logo, title);
        header.setAlignment(Pos.CENTER);

        // Mệnh giá dạng "card"
        VBox optionBox = new VBox(15);
        optionBox.setAlignment(Pos.CENTER);

        ToggleGroup group = new ToggleGroup();

        optionBox.getChildren().addAll(
                createMoneyOption("5 USD (~120,000 VNĐ)", 5, group),
                createMoneyOption("10 USD (~240,000 VNĐ)", 10, group),
                createMoneyOption("20 USD (~480,000 VNĐ)", 20, group)
        );

        // Button nạp tiền
        Button payBtn = new Button("Nạp tiền ngay");
        payBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #0070BA; -fx-text-fill: white;");
        payBtn.setPadding(new Insets(10, 20, 10, 20));
        payBtn.setOnAction(e -> {
            Toggle selected = group.getSelectedToggle();
            if (selected != null) {
                int amount = (int) selected.getUserData();
                try {
                    PayPalService payPalService = new PayPalService();
                    String approvalLink = payPalService.createPayment(
                            (double) amount,
                            "USD",
                            "paypal",
                            "sale",
                            "Nạp tiền vào tài khoản",
                            "https://af63da65b6f3.ngrok-free.app/cancel",
                            "https://af63da65b6f3.ngrok-free.app/success?amount=" + amount
                    );
                    System.out.println("Link thanh toán PayPal: " + approvalLink);

                    // Sau khi mở link PayPal
                    if (approvalLink != null) {
                        Desktop.getDesktop().browse(new URI(approvalLink)); // Mở PayPal

                        // ✅ Tạo 1 thread để đợi callback server thông báo thành công
                        new Thread(() -> {
                            while (!WebServer.paymentDone) {
                                try {
                                    Thread.sleep(2000); // Đợi 2 giây mỗi lần
                                } catch (InterruptedException ignored) {}
                            }

                            // ✅ Khi nạp thành công thì trở lại giao diện ProfileScene
                            javafx.application.Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nạp tiền thành công!");
                                alert.showAndWait();

                                try {
                                    ProfileScene.showProfile((Stage) payBtn.getScene().getWindow(), WebServer.getLoggedInUser());

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                WebServer.paymentDone = false; // reset lại cho lần sau
                            });
                        }).start();
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Lỗi khi tạo giao dịch PayPal!");
                }
            } else {
                showError("Vui lòng chọn mệnh giá!");
            }
        });

        VBox root = new VBox(30, header, optionBox, payBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setBackground(new Background(new BackgroundFill(Color.web("#f0f4f8"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    // Tạo "card" cho mệnh giá
    private HBox createMoneyOption(String label, int value, ToggleGroup group) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setUserData(value);
        rb.setFont(Font.font(16));
        rb.setTextFill(Color.web("#333"));
        rb.setSelected(value == 5);

        HBox box = new HBox(rb);
        box.setPadding(new Insets(10));
        box.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
        box.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        box.setEffect(new DropShadow(2, Color.gray(0.6)));
        box.setPrefWidth(300);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
