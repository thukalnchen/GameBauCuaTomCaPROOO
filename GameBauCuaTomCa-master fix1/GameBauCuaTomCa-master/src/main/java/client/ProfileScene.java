//pro5
package client;

import common.User;
import db.UserDAO;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.control.ScrollPane;
import java.io.InputStream;
import java.util.Random;

public class ProfileScene {
    private static Timeline backgroundAnimation;
    private static Timeline particleAnimation;

    public static void showProfile(Stage stage, User user) {
        StackPane root = new StackPane();

        Rectangle bgRect = new Rectangle(450, 1200);
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.5, Color.web("#764ba2")),
                new Stop(1, Color.web("#f093fb"))
        );
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4facfe")),
                new Stop(0.5, Color.web("#00f2fe")),
                new Stop(1, Color.web("#43e97b"))
        );
        bgRect.setFill(gradient1);

        backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), gradient1)),
                new KeyFrame(Duration.seconds(3), new KeyValue(bgRect.fillProperty(), gradient2)),
                new KeyFrame(Duration.seconds(6), new KeyValue(bgRect.fillProperty(), gradient1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        Pane particlePane = new Pane();
        createFloatingParticles(particlePane);

        Label title = new Label("🎮 PROFILE NGƯỜI CHƠI 🎮");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 50px 0 10px 0;");

        Glow glow = new Glow();
        glow.setLevel(0.8);
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.web("#ffffff", 0.8));
        titleShadow.setRadius(15);
        titleShadow.setSpread(0.3);
        glow.setInput(titleShadow);
        title.setEffect(glow);

        ScaleTransition titleScale = new ScaleTransition(Duration.seconds(2), title);
        titleScale.setFromX(0.8);
        titleScale.setFromY(0.8);
        titleScale.setToX(1.1);
        titleScale.setToY(1.1);
        titleScale.setCycleCount(Timeline.INDEFINITE);
        titleScale.setAutoReverse(true);
        titleScale.play();

        // Load and display avatar
        int avatarNum = user.getAvatar();
        String imgPath = "/avatars/avatar" + avatarNum + ".jpg";
        InputStream is = ProfileScene.class.getResourceAsStream(imgPath);
        if (is == null) {
            imgPath = "/avatars/default.jpg";
            is = ProfileScene.class.getResourceAsStream(imgPath);
        }
        if (is == null) {
            System.out.println("❌ Không tìm thấy ảnh avatar!");
            return;
        }

        Image avatarImage = new Image(is, 100, 100, false, true); // KHÔNG scale tự động
        ImageView avatarView = new ImageView(avatarImage);
        avatarView.setFitWidth(100);
        avatarView.setFitHeight(100);

// Tạo mặt nạ tròn CÙNG kích thước
        Circle clip = new Circle(50);
        clip.centerXProperty().bind(avatarView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(avatarView.fitHeightProperty().divide(2));
        avatarView.setClip(clip);


        StackPane avatarBox = new StackPane(avatarView);
        avatarBox.setPrefSize(100, 100);

        Circle border = new Circle(55);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(3);
        border.setFill(Color.TRANSPARENT);

        StackPane avatarContainer = new StackPane(avatarBox, border);
        avatarContainer.setPrefSize(110, 110);
        avatarContainer.setAlignment(Pos.CENTER);

// Xoay avatar + border
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), avatarContainer);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();


        Label avatar = new Label("🎭 Avatar: " + user.getAvatar());
        Label name = new Label("👤 " + user.getName());
        Label username = new Label("🏷️ @" + user.getUsername());
        Label balance = new Label("💰 " + String.format("%,d", user.getBalance()) + " xu");

        String glassStyle = "-fx-font-size: 16px; -fx-text-fill: white; " +
                "-fx-padding: 12px 20px; -fx-font-weight: 600; " +
                "-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-background-radius: 25px; -fx-border-radius: 25px; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1px;";

        avatar.setStyle(glassStyle);
        name.setStyle(glassStyle + "-fx-font-size: 18px;");
        username.setStyle(glassStyle);
        balance.setStyle(glassStyle + "-fx-text-fill: #FFD700; -fx-font-size: 18px;");

        FadeTransition[] infoFades = new FadeTransition[4];
        Label[] infoLabels = {avatar, name, username, balance};
        for (int i = 0; i < infoLabels.length; i++) {
            infoLabels[i].setOpacity(0);
            infoFades[i] = new FadeTransition(Duration.seconds(0.8), infoLabels[i]);
            infoFades[i].setFromValue(0);
            infoFades[i].setToValue(1);
            infoFades[i].setDelay(Duration.seconds(0.3 * i));
            infoFades[i].play();
        }

        VBox infoBox = new VBox(12, avatar, name, username, balance);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(20));

        Button changeAvatar = createNeonButton("🎨 Đổi Avatar", "#ff6b6b", "#ee5a52");
        Button changePass = createNeonButton("🔒 Đổi mật khẩu", "#4ecdc4", "#45b7aa");
        Button quickPlayBtn = createNeonButton("⚡ Chơi nhanh", "#45b7d1", "#3a9bc1");
        Button onlinePlayBtn = createNeonButton("🌐 Chơi Online", "#96ceb4", "#aafac9");
        Button watchAd = createNeonButton("🎬 Xem quảng cáo", "#feca57", "#ff9f1a");
        Button createTable = createNeonButton("🎲 Tạo bàn", "#feca57", "#fd9644");
        Button logout = createNeonButton("🚪 Đăng xuất", "#ff9ff3", "#f368e0");

        watchAd.setOnAction(e -> {
            AdRewardScene.showAd(stage, user, () -> {
                balance.setText("💰 " + String.format("%,d", user.getBalance()) + " xu");
            });
        });


        logout.setOnAction(e -> {
            if (backgroundAnimation != null) backgroundAnimation.stop();
            if (particleAnimation != null) particleAnimation.stop();
            try {
                new LoginScene().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        quickPlayBtn.setOnAction(e -> {
            if (backgroundAnimation != null) backgroundAnimation.stop();
            if (particleAnimation != null) particleAnimation.stop();
            new GameTableScene(user, stage).start(stage);
        });

        onlinePlayBtn.setOnAction(e -> {
            if (backgroundAnimation != null) backgroundAnimation.stop();
            if (particleAnimation != null) particleAnimation.stop();
            new GameTableOnlineScene(user).start(stage);
        });




        changePass.setOnAction(e -> showChangePasswordDialog(stage, user));

        changeAvatar.setOnAction(e -> showChangeAvatarDialog(stage, user, () -> showProfile(stage, user)));

        VBox gameButtons = new VBox(15, quickPlayBtn, onlinePlayBtn, createTable);
        gameButtons.setAlignment(Pos.CENTER);

        VBox settingButtons = new VBox(15, changeAvatar, changePass);
        settingButtons.setAlignment(Pos.CENTER);
        settingButtons.getChildren().add(watchAd);


        TranslateTransition gameSlide = new TranslateTransition(Duration.seconds(1), gameButtons);
        gameSlide.setFromX(-200);
        gameSlide.setToX(0);
        gameSlide.setDelay(Duration.seconds(1));
        gameSlide.play();

        TranslateTransition settingSlide = new TranslateTransition(Duration.seconds(1), settingButtons);
        settingSlide.setFromX(200);
        settingSlide.setToX(0);
        settingSlide.setDelay(Duration.seconds(1.2));
        settingSlide.play();

        Region spacer = new Region();
        spacer.setPrefHeight(20);

        VBox content = new VBox(20, title, avatarContainer, infoBox, gameButtons, settingButtons, spacer, logout);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

// Gắn ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

// Tùy chọn để scroll mượt
        scrollPane.setPannable(true);

        root.getChildren().addAll(bgRect, particlePane, scrollPane);


        Scene scene = new Scene(root, 450, 600);
        stage.setTitle("🎮 Bầu Cua - Profile");
        stage.setScene(scene);
        stage.show();


    }


    private static Button createNeonButton(String text, String color1, String color2) {
        Button button = new Button(text);
        button.setPrefWidth(220);
        button.setPrefHeight(45);

        String baseStyle = "-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); " +
                "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-background-radius: 25px; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2px; " +
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
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();

            // Tăng glow
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(Color.web(color1, 0.8));
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

    private static void createFloatingParticles(Pane particlePane) {
        Random random = new Random();

        // Tạo 20 particles
        for (int i = 0; i < 20; i++) {
            Circle particle = new Circle(random.nextDouble() * 3 + 1);
            particle.setFill(Color.web("#ffffff", random.nextDouble() * 0.6 + 0.2));
            particle.setLayoutX(random.nextDouble() * 450);
            particle.setLayoutY(random.nextDouble() * 600);

            // Animation bay lượn
            TranslateTransition move = new TranslateTransition(
                    Duration.seconds(random.nextDouble() * 10 + 5), particle);
            move.setByX(random.nextDouble() * 200 - 100);
            move.setByY(random.nextDouble() * 200 - 100);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            // Animation fade
            FadeTransition fade = new FadeTransition(
                    Duration.seconds(random.nextDouble() * 3 + 2), particle);
            fade.setFromValue(0.2);
            fade.setToValue(0.8);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            particlePane.getChildren().add(particle);
        }
    }

    public static void showChangeAvatarDialog(Stage owner, User user, Runnable onSuccess) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.setTitle("🎭 Chọn avatar mới");

        // Background với hiệu ứng
        StackPane root = new StackPane();
        Rectangle bg = new Rectangle(550, 250);
        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(1, Color.web("#764ba2"))
        ));

        Label titleLabel = new Label("🎨 Chọn avatar yêu thích của bạn:");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-padding: 15px;");

        // Hiệu ứng glow cho title
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#ffffff", 0.8));
        titleGlow.setRadius(10);
        titleLabel.setEffect(titleGlow);

        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(25));

        int avatarCount = 5;
        for (int i = 0; i < avatarCount; i++) {
            String imgPath = "/avatars/avatar" + i + ".jpg";
            InputStream is = ProfileScene.class.getResourceAsStream(imgPath);
            Image img = new Image(is, 80, 80, false, true); // Không giữ tỉ lệ
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(80);
            imgView.setFitHeight(80);

// Tạo mặt nạ tròn chính xác giữa ảnh
            Circle clip = new Circle(40);
            clip.centerXProperty().bind(imgView.fitWidthProperty().divide(2));
            clip.centerYProperty().bind(imgView.fitHeightProperty().divide(2));
            imgView.setClip(clip);


            // Container với viền
            StackPane avatarContainer = new StackPane();
            Circle border = new Circle(45);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.web("#ffffff", 0.6));
            border.setStrokeWidth(3);

            avatarContainer.getChildren().addAll(border, imgView);

            int idx = i;

            // Hover effects
            avatarContainer.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), avatarContainer);
                scale.setToX(1.2);
                scale.setToY(1.2);
                scale.play();

                border.setStroke(Color.web("#FFD700"));
                border.setStrokeWidth(4);

                DropShadow glow = new DropShadow();
                glow.setColor(Color.web("#FFD700", 0.8));
                glow.setRadius(20);
                avatarContainer.setEffect(glow);
            });

            avatarContainer.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), avatarContainer);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                border.setStroke(Color.web("#ffffff", 0.6));
                border.setStrokeWidth(3);
                avatarContainer.setEffect(null);
            });

            avatarContainer.setOnMouseClicked(e -> {
                if (UserDAO.updateAvatar(user.getId(), idx)) {
                    user.setAvatar(idx);

                    // Hiệu ứng khi chọn thành công
                    FadeTransition success = new FadeTransition(Duration.millis(300), avatarContainer);
                    success.setFromValue(1.0);
                    success.setToValue(0.3);
                    success.setCycleCount(2);
                    success.setAutoReverse(true);
                    success.setOnFinished(ev -> {
                        dialog.close();
                        if (onSuccess != null) onSuccess.run();
                    });
                    success.play();
                }
            });

            // Animation xuất hiện
            avatarContainer.setOpacity(0);
            FadeTransition appear = new FadeTransition(Duration.millis(500), avatarContainer);
            appear.setFromValue(0);
            appear.setToValue(1);
            appear.setDelay(Duration.millis(i * 100));
            appear.play();

            avatarBox.getChildren().add(avatarContainer);
        }

        VBox content = new VBox(25, titleLabel, avatarBox);
        content.setAlignment(Pos.CENTER);

        root.getChildren().addAll(bg, content);

        Scene scene = new Scene(root, 550, 250);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showChangePasswordDialog(Stage owner, User user) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.setTitle("🔒 Đổi mật khẩu");

        // Background với hiệu ứng
        StackPane root = new StackPane();
        Rectangle bg = new Rectangle(450, 400);
        bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4facfe")),
                new Stop(1, Color.web("#00f2fe"))
        ));

        Label titleLabel = new Label("🔐 Tạo mật khẩu mới cho tài khoản");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-padding: 15px;");

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#ffffff", 0.8));
        titleGlow.setRadius(10);
        titleLabel.setEffect(titleGlow);

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("🔑 Nhập mật khẩu mới...");
        newPassField.setPrefHeight(45);
        newPassField.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                "-fx-background-radius: 25px; -fx-border-radius: 25px; " +
                "-fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2px; " +
                "-fx-padding: 12px 20px; -fx-font-size: 14px;");

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("✅ Nhập lại mật khẩu mới...");
        confirmPassField.setPrefHeight(45);
        confirmPassField.setStyle(newPassField.getStyle());

        Button submit = createNeonButton("🔄 Cập nhật mật khẩu", "#27ae60", "#229954");
        Button cancel = createNeonButton("❌ Hủy bỏ", "#e74c3c", "#c0392b");

        Label msg = new Label();
        msg.setStyle("-fx-font-size: 14px; -fx-padding: 12px 20px; " +
                "-fx-background-radius: 20px; -fx-font-weight: bold;");

        submit.setOnAction(e -> {
            String newPass = newPassField.getText().trim();
            String confirmPass = confirmPassField.getText().trim();

            if (newPass.isEmpty()) {
                showMessage(msg, "⚠️ Vui lòng nhập mật khẩu mới!", "#f39c12");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showMessage(msg, "❌ Mật khẩu không khớp! Vui lòng thử lại.", "#e74c3c");
                return;
            }

            if (newPass.length() < 6) {
                showMessage(msg, "🔒 Mật khẩu phải có ít nhất 6 ký tự!", "#f39c12");
                return;
            }

            if (UserDAO.changePassword(user.getId(), newPass)) {
                showMessage(msg, "🎉 Đổi mật khẩu thành công!", "#27ae60");

                // Hiệu ứng thành công
                Timeline successEffect = new Timeline(
                        new KeyFrame(Duration.seconds(1.5), ev -> dialog.close())
                );
                successEffect.play();
            } else {
                showMessage(msg, "💥 Đổi mật khẩu thất bại! Vui lòng thử lại.", "#e74c3c");
            }
        });

        cancel.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(20, submit, cancel);
        buttonBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(25, titleLabel, newPassField, confirmPassField, buttonBox, msg);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        // Animation xuất hiện cho các elements
        content.getChildren().forEach(node -> {
            node.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(600), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(content.getChildren().indexOf(node) * 150));
            fade.play();
        });

        root.getChildren().addAll(bg, content);

        Scene scene = new Scene(root, 450, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void showMessage(Label msg, String text, String color) {
        msg.setText(text);
        msg.setStyle(msg.getStyle() + "-fx-text-fill: white; " +
                "-fx-background-color: " + color + ";");

        // Hiệu ứng bounce
        ScaleTransition bounce = new ScaleTransition(Duration.millis(300), msg);
        bounce.setFromX(0.8);
        bounce.setFromY(0.8);
        bounce.setToX(1.0);
        bounce.setToY(1.0);
        bounce.play();
    }
}