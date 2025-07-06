package client;

import common.User;
import db.UserDAO;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class RegisterScene extends Application {
    private int selectedAvatar = -1;
    private Timeline backgroundAnimation;
    private Timeline particleAnimation;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();

        // NỀN GRADIENT ĐỘNG
        Rectangle bgRect = new Rectangle(450, 1200);
        LinearGradient g1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.3, Color.web("#764ba2")),
                new Stop(0.7, Color.web("#f093fb")),
                new Stop(1, Color.web("#f5576c"))
        );
        LinearGradient g2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4facfe")),
                new Stop(0.3, Color.web("#00f2fe")),
                new Stop(0.7, Color.web("#43e97b")),
                new Stop(1, Color.web("#38f9d7"))
        );
        LinearGradient g3 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fa709a")),
                new Stop(0.5, Color.web("#fee140")),
                new Stop(1, Color.web("#fa709a"))
        );
        bgRect.setFill(g1);

        backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), g1)),
                new KeyFrame(Duration.seconds(4), new KeyValue(bgRect.fillProperty(), g2)),
                new KeyFrame(Duration.seconds(8), new KeyValue(bgRect.fillProperty(), g3)),
                new KeyFrame(Duration.seconds(12), new KeyValue(bgRect.fillProperty(), g1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        Pane particlePane = new Pane();
        createFloatingParticles(particlePane);

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(30));
        vbox.setMaxWidth(380);
        vbox.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 30px; -fx-border-color: rgba(255,255,255,0.8); " +
                "-fx-border-width: 2px; -fx-border-radius: 30px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(20);
        shadow.setSpread(0.1);
        vbox.setEffect(shadow);

        Label title = new Label("📝 ĐĂNG KÝ TÀI KHOẢN");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField usernameField = styledField("👤 Nhập username...");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Nhập mật khẩu...");
        passwordField.setPrefHeight(50);
        passwordField.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 25px; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 2px; -fx-border-radius: 25px; " +
                "-fx-padding: 10px 20px; -fx-font-size: 14px;");

        DropShadow errorShadow = new DropShadow();
        errorShadow.setColor(Color.web("#e74c3c"));
        errorShadow.setRadius(10);
        errorShadow.setSpread(0.3);

        Label passwordHint = new Label("Hãy nhập ít nhất 7 ký tự");
        passwordHint.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        passwordHint.setVisible(false);

        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            boolean isInvalid = newText.length() < 6;
            passwordHint.setVisible(isInvalid);
            if (isInvalid) {
                passwordField.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-color: #e74c3c; -fx-border-width: 2px; " +
                        "-fx-border-radius: 25px; -fx-padding: 10px 20px; " +
                        "-fx-font-size: 14px;");
                passwordField.setEffect(errorShadow);
            } else {
                passwordField.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-color: #bdc3c7; -fx-border-width: 2px; " +
                        "-fx-border-radius: 25px; -fx-padding: 10px 20px; " +
                        "-fx-font-size: 14px;");
                passwordField.setEffect(null);
            }
        });

        TextField nameField = styledField("📛 Nhập họ tên...");

        Label avatarLabel = new Label("Chọn avatar:");
        avatarLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        HBox avatarBox = new HBox(10);
        avatarBox.setAlignment(Pos.CENTER);
        int avatarCount = 5;
        ImageView[] avatarViews = new ImageView[avatarCount];

        for (int i = 0; i < avatarCount; i++) {
            String imgPath = "/avatars/avatar" + i + ".jpg";
            Image img = new Image(getClass().getResourceAsStream(imgPath), 64, 64, true, true);
            ImageView imgView = new ImageView(img);
            imgView.setStyle("-fx-border-color: transparent; -fx-border-width: 3px; -fx-cursor: hand; -fx-background-radius: 50%;");
            int idx = i;
            imgView.setOnMouseClicked(e -> {
                for (ImageView av : avatarViews) {
                    av.setStyle("-fx-border-color: transparent; -fx-border-width: 3px; -fx-effect: null;");
                    av.setScaleX(1.0);
                    av.setScaleY(1.0);
                }
                imgView.setStyle("-fx-border-color: #3498db; -fx-border-width: 3px; -fx-border-radius: 10px;");
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web("#3498db"));
                glow.setRadius(20);
                glow.setSpread(0.3);
                imgView.setEffect(glow);
                imgView.setScaleX(1.1);
                imgView.setScaleY(1.1);
                selectedAvatar = idx;
            });
            avatarViews[i] = imgView;
            avatarBox.getChildren().add(imgView);
        }

        Button registerBtn = createNeonButton("✅ ĐĂNG KÝ", "#27ae60", "#2ecc71");
        Label result = new Label();
        result.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        result.setWrapText(true);
        result.setMaxWidth(300);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String name = nameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || selectedAvatar == -1) {
                result.setText("⚠️ Vui lòng nhập đầy đủ thông tin và chọn avatar!");
                return;
            }
            if (password.length() < 6) {
                result.setText("⚠️ Mật khẩu phải có ít nhất 6 ký tự!");
                passwordHint.setVisible(true);
                return;
            }

            User user = new User(0, username, password, name, selectedAvatar, 10000);
            boolean ok = UserDAO.register(user);
            if (ok) {
                result.setText("🎉 Đăng ký thành công! Đang chuyển đến trang Profile...");
                backgroundAnimation.stop();
                particleAnimation.stop();
                ProfileScene.showProfile(stage, user);
            } else {
                result.setText("❌ Đăng ký thất bại! Username có thể đã tồn tại.");
            }
        });

        vbox.getChildren().addAll(title, usernameField, passwordField, passwordHint, nameField, avatarLabel, avatarBox, registerBtn, result);

        vbox.setOpacity(0);
        vbox.setScaleX(0.8);
        vbox.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(Duration.seconds(1), vbox);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1), vbox);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();

        root.getChildren().addAll(bgRect, particlePane, vbox);
        Scene scene = new Scene(root, 440, 500);
        stage.setTitle("🎮 Bầu Cua - Đăng ký");
        stage.setScene(scene);
        stage.show();
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(50);
        tf.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 25px; -fx-border-color: #bdc3c7; -fx-border-width: 2px; " +
                "-fx-border-radius: 25px; -fx-padding: 10px 20px; -fx-font-size: 15px;");
        return tf;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(50);
        pf.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 25px; -fx-border-color: #bdc3c7; -fx-border-width: 2px; " +
                "-fx-border-radius: 25px; -fx-padding: 10px 20px; -fx-font-size: 15px;");
        return pf;
    }

    private Button createNeonButton(String text, String color1, String color2) {
        Button button = new Button(text);
        button.setPrefWidth(280);
        button.setPrefHeight(50);
        String style = "-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 25px; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 2px; " +
                "-fx-border-radius: 25px;";
        button.setStyle(style);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color1, 0.6));
        glow.setRadius(15);
        glow.setSpread(0.3);
        button.setEffect(glow);

        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(Color.web(color1, 0.9));
            hoverGlow.setRadius(25);
            hoverGlow.setSpread(0.5);
            button.setEffect(hoverGlow);
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            button.setEffect(glow);
        });

        return button;
    }

    private void createFloatingParticles(Pane particlePane) {
        Random random = new Random();
        for (int i = 0; i < 25; i++) {
            Circle particle = new Circle(random.nextDouble() * 4 + 1);
            particle.setFill(Color.web("#ffffff", random.nextDouble() * 0.7 + 0.3));
            particle.setLayoutX(random.nextDouble() * 400);
            particle.setLayoutY(random.nextDouble() * 500);

            TranslateTransition move = new TranslateTransition(Duration.seconds(random.nextDouble() * 15 + 10), particle);
            move.setByX(random.nextDouble() * 300 - 150);
            move.setByY(random.nextDouble() * 300 - 150);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            FadeTransition fade = new FadeTransition(Duration.seconds(random.nextDouble() * 4 + 3), particle);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            ScaleTransition scale = new ScaleTransition(Duration.seconds(random.nextDouble() * 3 + 2), particle);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(1.5);
            scale.setToY(1.5);
            scale.setCycleCount(Timeline.INDEFINITE);
            scale.setAutoReverse(true);
            scale.play();

            particlePane.getChildren().add(particle);
        }
        particleAnimation = new Timeline(); // placeholder để gọi stop()
    }

    public static void main(String[] args) {
        launch(args);
    }
}
