package client;

import common.User;
import db.UserDAO;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.WebServer;

import java.io.IOException;
import java.util.Random;

public class LoginScene extends Application {
    private Timeline backgroundAnimation;
    private Timeline particleAnimation;

    @Override
    public void start(Stage stage) {
        // Tạo root container
        StackPane root = new StackPane();

        // Background gradient động
        Rectangle bgRect = new Rectangle(450, 1200);
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.3, Color.web("#764ba2")),
                new Stop(0.7, Color.web("#f093fb")),
                new Stop(1, Color.web("#f5576c"))
        );
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4facfe")),
                new Stop(0.3, Color.web("#00f2fe")),
                new Stop(0.7, Color.web("#43e97b")),
                new Stop(1, Color.web("#38f9d7"))
        );
        LinearGradient gradient3 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fa709a")),
                new Stop(0.5, Color.web("#fee140")),
                new Stop(1, Color.web("#fa709a"))
        );

        bgRect.setFill(gradient1);

        // Animation cho background
        backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), gradient1)),
                new KeyFrame(Duration.seconds(4), new KeyValue(bgRect.fillProperty(), gradient2)),
                new KeyFrame(Duration.seconds(8), new KeyValue(bgRect.fillProperty(), gradient3)),
                new KeyFrame(Duration.seconds(12), new KeyValue(bgRect.fillProperty(), gradient1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        // Tạo particles bay lượn
        Pane particlePane = new Pane();
        createFloatingParticles(particlePane);

        // Container chính với glass effect KHÔNG MỜ
        VBox loginContainer = new VBox(25);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(40));
        loginContainer.setMaxWidth(350);
        loginContainer.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-background-radius: 30px; " +
                "-fx-border-color: rgba(255,255,255,0.8); " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 30px;");

        // BỎ hiệu ứng blur - để container rõ ràng
        DropShadow containerShadow = new DropShadow();
        containerShadow.setColor(Color.web("#000000", 0.3));
        containerShadow.setRadius(20);
        containerShadow.setSpread(0.1);
        loginContainer.setEffect(containerShadow);

        // Title với hiệu ứng glow
        Label title = new Label("🎮 ĐĂNG NHẬP 🎮");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-text-fill: #2c3e50; -fx-padding: 0 0 20px 0;");

        // Hiệu ứng glow cho title
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#3498db", 0.6));
        titleGlow.setRadius(15);
        titleGlow.setSpread(0.3);
        title.setEffect(titleGlow);

        // Animation cho title
        ScaleTransition titleScale = new ScaleTransition(Duration.seconds(2), title);
        titleScale.setFromX(0.9);
        titleScale.setFromY(0.9);
        titleScale.setToX(1.1);
        titleScale.setToY(1.1);
        titleScale.setCycleCount(Timeline.INDEFINITE);
        titleScale.setAutoReverse(true);
        titleScale.play();

        // Rotate animation cho title
        RotateTransition titleRotate = new RotateTransition(Duration.seconds(20), title);
        titleRotate.setByAngle(5);
        titleRotate.setCycleCount(Timeline.INDEFINITE);
        titleRotate.setAutoReverse(true);
        titleRotate.play();

        // Username field với style rõ ràng
        TextField usernameField = new TextField();
        usernameField.setPromptText("👤 Nhập username của bạn...");
        usernameField.setPrefHeight(50);
        usernameField.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 25px; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-border-width: 2px; -fx-border-radius: 25px; " +
                "-fx-padding: 15px 20px; -fx-font-size: 16px; " +
                "-fx-text-fill: #2c3e50;");

        // Focus effects cho username field
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-color: #3498db; -fx-border-width: 3px; " +
                        "-fx-border-radius: 25px; " +
                        "-fx-padding: 15px 20px; -fx-font-size: 16px; " +
                        "-fx-text-fill: #2c3e50;");

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), usernameField);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            } else {
                usernameField.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-color: #bdc3c7; " +
                        "-fx-border-width: 2px; -fx-border-radius: 25px; " +
                        "-fx-padding: 15px 20px; -fx-font-size: 16px; " +
                        "-fx-text-fill: #2c3e50;");

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), usernameField);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            }
        });

        // Password field với style rõ ràng
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Nhập mật khẩu của bạn...");
        passwordField.setPrefHeight(50);
        passwordField.setStyle(usernameField.getStyle());

        // Focus effects cho password field
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 25px; " +
                        "-fx-border-color: #e74c3c; -fx-border-width: 3px; " +
                        "-fx-border-radius: 25px; " +
                        "-fx-padding: 15px 20px; -fx-font-size: 16px; " +
                        "-fx-text-fill: #2c3e50;");

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), passwordField);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            } else {
                passwordField.setStyle(usernameField.getStyle());

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), passwordField);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            }
        });

        // Login button với hiệu ứng neon
        Button loginBtn = createNeonButton("🚀 ĐĂNG NHẬP", "#27ae60", "#2ecc71");
        Button gotoRegister = createNeonButton("📝 ĐĂNG KÝ TÀI KHOẢN MỚI", "#3498db", "#5dade2");

        // Result label với style rõ ràng
        Label result = new Label();
        result.setStyle("-fx-font-size: 14px; -fx-padding: 15px 20px; " +
                "-fx-background-radius: 20px; -fx-font-weight: bold; " +
                "-fx-text-alignment: center;");
        result.setWrapText(true);
        result.setMaxWidth(300);

        // Event handlers
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showMessage(result, "⚠️ Vui lòng nhập đầy đủ username và password!", "#f39c12");
                shakeAnimation(loginContainer);
                return;
            }

            // Hiệu ứng loading
            loginBtn.setText("🔄 Đang đăng nhập...");
            loginBtn.setDisable(true);

            // Simulate loading delay
            Timeline loadingDelay = new Timeline(
                    new KeyFrame(Duration.seconds(1), ev -> {
                        User user = UserDAO.login(username, password);
                        if (user != null) {
                            showMessage(result, "🎉 Đăng nhập thành công! Chào mừng " + user.getName() + "!", "#27ae60");

                            // Hiệu ứng thành công
                            successAnimation(loginContainer, () -> {
                                // Dừng animations trước khi chuyển scene
                                if (backgroundAnimation != null) backgroundAnimation.stop();
                                if (particleAnimation != null) particleAnimation.stop();
                                // Khởi động WebServer với user sau khi login thành công
                                try {
                                    WebServer.start(user);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }

                                // Sau đó chuyển sang profile
                                ProfileScene.showProfile(stage, user);

                            });
                        } else {
                            showMessage(result, "❌ Đăng nhập thất bại! Kiểm tra lại thông tin.", "#e74c3c");
                            shakeAnimation(loginContainer);
                            loginBtn.setText("🚀 ĐĂNG NHẬP");
                            loginBtn.setDisable(false);
                        }
                    })
            );
            loadingDelay.play();
        });

        gotoRegister.setOnAction(e -> {
            // Hiệu ứng fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                // Dừng animations
                if (backgroundAnimation != null) backgroundAnimation.stop();
                if (particleAnimation != null) particleAnimation.stop();

                RegisterScene register = new RegisterScene();
                try {
                    register.start(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        });

        // Enter key support
        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Thêm các elements vào container
        loginContainer.getChildren().addAll(title, usernameField, passwordField, loginBtn, gotoRegister, result);

        // Animation xuất hiện cho container
        loginContainer.setOpacity(0);
        loginContainer.setScaleX(0.8);
        loginContainer.setScaleY(0.8);

        FadeTransition containerFade = new FadeTransition(Duration.seconds(1), loginContainer);
        containerFade.setFromValue(0);
        containerFade.setToValue(1);

        ScaleTransition containerScale = new ScaleTransition(Duration.seconds(1), loginContainer);
        containerScale.setFromX(0.8);
        containerScale.setFromY(0.8);
        containerScale.setToX(1.0);
        containerScale.setToY(1.0);

        ParallelTransition containerAppear = new ParallelTransition(containerFade, containerScale);
        containerAppear.setDelay(Duration.millis(500));
        containerAppear.play();

        // Thêm tất cả vào root
        root.getChildren().addAll(bgRect, particlePane, loginContainer);

        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("🎮 Bầu Cua - Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    private Button createNeonButton(String text, String color1, String color2) {
        Button button = new Button(text);
        button.setPrefWidth(280);
        button.setPrefHeight(50);

        String baseStyle = "-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 25px; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 2px; " +
                "-fx-border-radius: 25px;";

        button.setStyle(baseStyle);

        // Hiệu ứng glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color1, 0.6));
        glow.setRadius(15);
        glow.setSpread(0.3);
        button.setEffect(glow);

        // Hover effects
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            // Tăng glow
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

        // Click effect
        button.setOnMousePressed(e -> {
            ScaleTransition click = new ScaleTransition(Duration.millis(100), button);
            click.setToX(0.95);
            click.setToY(0.95);
            click.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
            release.setToX(1.05);
            release.setToY(1.05);
            release.play();
        });

        return button;
    }

    private void createFloatingParticles(Pane particlePane) {
        Random random = new Random();

        // Tạo 25 particles
        for (int i = 0; i < 25; i++) {
            Circle particle = new Circle(random.nextDouble() * 4 + 1);
            particle.setFill(Color.web("#ffffff", random.nextDouble() * 0.7 + 0.3));
            particle.setLayoutX(random.nextDouble() * 400);
            particle.setLayoutY(random.nextDouble() * 500);

            // Animation bay lượn
            TranslateTransition move = new TranslateTransition(
                    Duration.seconds(random.nextDouble() * 15 + 10), particle);
            move.setByX(random.nextDouble() * 300 - 150);
            move.setByY(random.nextDouble() * 300 - 150);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            // Animation fade
            FadeTransition fade = new FadeTransition(
                    Duration.seconds(random.nextDouble() * 4 + 3), particle);
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            // Animation scale
            ScaleTransition scale = new ScaleTransition(
                    Duration.seconds(random.nextDouble() * 3 + 2), particle);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(1.5);
            scale.setToY(1.5);
            scale.setCycleCount(Timeline.INDEFINITE);
            scale.setAutoReverse(true);
            scale.play();

            particlePane.getChildren().add(particle);
        }
    }

    private void showMessage(Label msg, String text, String color) {
        msg.setText(text);
        msg.setStyle(msg.getStyle() + "-fx-text-fill: white; " +
                "-fx-background-color: " + color + ";");

        // Hiệu ứng bounce
        msg.setOpacity(0);
        msg.setScaleX(0.5);
        msg.setScaleY(0.5);

        FadeTransition fade = new FadeTransition(Duration.millis(300), msg);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), msg);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition appear = new ParallelTransition(fade, scale);
        appear.play();
    }

    private void shakeAnimation(VBox container) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), container);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void successAnimation(VBox container, Runnable onFinished) {
        // Hiệu ứng xanh lá
        Timeline greenFlash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(container.styleProperty(),
                        container.getStyle())),
                new KeyFrame(Duration.millis(200), new KeyValue(container.styleProperty(),
                        container.getStyle() + "-fx-border-color: #27ae60; -fx-border-width: 4px;")),
                new KeyFrame(Duration.millis(400), new KeyValue(container.styleProperty(),
                        container.getStyle()))
        );

        greenFlash.setOnFinished(e -> {
            Timeline delay = new Timeline(
                    new KeyFrame(Duration.seconds(1.5), ev -> onFinished.run())
            );
            delay.play();
        });

        greenFlash.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}