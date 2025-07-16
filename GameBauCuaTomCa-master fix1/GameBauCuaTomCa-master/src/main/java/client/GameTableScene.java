package client;

import common.Player;
import common.User;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import javafx.scene.media.AudioClip;
import javafx.scene.effect.*;

import java.util.*;

public class GameTableScene {
    private ImageView[] symbolsImageViews = new ImageView[6];
    private Timeline backgroundAnimation;

    private final String[] symbolNames = {"Nai", "Bầu", "Gà", "Cá", "Cua", "Tôm"};
    private AudioClip shakeSound;
    private AudioClip winSound;
    private AudioClip loseSound;
    private AudioClip backsound;

    private final String[] symbolImg = {
            "/symbols/nai.png", "/symbols/bau.png", "/symbols/ga.png",
            "/symbols/ca.png", "/symbols/cua.png", "/symbols/tom.png"
    };

    private Player player = new Player("Bạn", 0, 1000000);
    Player bot1 = new Player("Arian Shirone", 1, 520000);
    Player bot2 = new Player("Army Kamis", 2, 520000);
    Player bot3 = new Player("Chen Thukaln", 3, 291205);
    private Player[] bots = {bot1, bot2, bot3};

    private int[] betAmount = new int[6];
    private int[][] botBets = new int[3][6];

    Label playerBalanceLabel = new Label();
    Label bot1BalanceLabel = new Label();
    Label bot2BalanceLabel = new Label();
    Label bot3BalanceLabel = new Label();

    private User user;
    private Stage mainStage;

    public GameTableScene(User user, Stage stage) {
        this.user = user;
        this.mainStage = stage;
    }

    private VBox buildCompactPlayerBox(Player p, String avatarPath, Label balanceLabel, String position) {
        // Avatar vuông nhỏ gọn
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream(avatarPath), 50, 50, true, true));

        Rectangle avatarClip = new Rectangle(50, 50);
        avatarClip.setArcWidth(10);
        avatarClip.setArcHeight(10);
        img.setClip(avatarClip);

        // Container avatar compact
        StackPane avatarContainer = new StackPane();
        Rectangle avatarBorder = new Rectangle(55, 55);
        avatarBorder.setArcWidth(12);
        avatarBorder.setArcHeight(12);
        avatarBorder.setFill(Color.TRANSPARENT);
        avatarBorder.setStroke(Color.web("#FFD700", 0.9));
        avatarBorder.setStrokeWidth(2);

        // Glow nhẹ
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setColor(Color.web("#FFD700", 0.4));
        avatarGlow.setRadius(8);
        img.setEffect(avatarGlow);

        avatarContainer.getChildren().addAll(avatarBorder, img);

        // Name label compact
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-padding: 2px;");

        // Balance label compact
        balanceLabel.setText(String.format("%,d", p.getBalance()));
        balanceLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #FFD700; " +
                "-fx-font-weight: bold; -fx-padding: 2px 6px; " +
                "-fx-background-color: rgba(0,0,0,0.6); " +
                "-fx-background-radius: 8px;");

        VBox box = new VBox(4, avatarContainer, name, balanceLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(8));
        box.setPrefWidth(80);
        box.setPrefHeight(120);

        // Style theo vị trí
        String boxStyle = "-fx-background-color: rgba(255,255,255,0.12); " +
                "-fx-background-radius: 12px; " + "-fx-border-width: 1px; -fx-border-radius: 12px;";

        switch(position) { //colorStyle
            case "bottom":
                boxStyle += "-fx-border-color: rgba(46, 1111, 113, 0.7);";
                break;
            case "left":
                boxStyle += "-fx-border-color: rgba(52, 152, 219, 0.7);";
                break;
            case "top":
                boxStyle += "-fx-border-color: rgba(155, 89, 182, 0.7);";
                break;
            case "right":
                boxStyle += "-fx-border-color: rgba(231, 76, 60, 0.7);";
                break;
        }

        box.setStyle(boxStyle);
        return box;
    }

    private void botsPlaceBet() {
        Random rand = new Random();
        for (int b = 0; b < bots.length; b++) {
            Player bot = bots[b];
            int[] bets = new int[6];
            int betTimes = rand.nextInt(2) + 1;
            for (int i = 0; i < betTimes; i++) {
                int idx = rand.nextInt(6);
                int amount = Math.min(1000 * (rand.nextInt(5) + 1), bot.getBalance());
                if (amount > 0) {
                    bets[idx] += amount;
                    bot.setBalance(bot.getBalance() - amount);
                }
            }
            botBets[b] = bets;
        }
    }

    private void createFloatingParticles(Pane particlePane) {
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            Circle particle = new Circle(random.nextDouble() * 2 + 1);
            particle.setFill(Color.web("#FFD700", random.nextDouble() * 0.5 + 0.2));
            particle.setLayoutX(random.nextDouble() * 1200);
            particle.setLayoutY(random.nextDouble() * 800);

            TranslateTransition move = new TranslateTransition(
                    Duration.seconds(random.nextDouble() * 25 + 20), particle);
            move.setByX(random.nextDouble() * 300 - 150);
            move.setByY(random.nextDouble() * 300 - 150);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            FadeTransition fade = new FadeTransition(
                    Duration.seconds(random.nextDouble() * 5 + 4), particle);
            fade.setFromValue(0.1);
            fade.setToValue(0.6);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            particlePane.getChildren().add(particle);
        }
    }

    private Button createStyledButton(String text, String color1, String color2, double width, double height) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setPrefHeight(height);

        String baseStyle = "-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); " +
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 20px; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1px; " +
                "-fx-border-radius: 20px;";

        button.setStyle(baseStyle);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color1, 0.5));
        glow.setRadius(10);
        button.setEffect(glow);

        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }




    public void start(Stage stage) {
        // Root container
        StackPane root = new StackPane();

        // Background gradient
        Rectangle bgRect = new Rectangle(1600, 800);
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0f4c75")),
                new Stop(0.3, Color.web("#3282b8")),
                new Stop(0.7, Color.web("#bbe1fa")),
                new Stop(1, Color.web("#1e3c72"))
        );
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.3, Color.web("#764ba2")),
                new Stop(0.7, Color.web("#f093fb")),
                new Stop(1, Color.web("#f5576c"))
        );

        bgRect.setFill(gradient1);

        backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), gradient1)),
                new KeyFrame(Duration.seconds(8), new KeyValue(bgRect.fillProperty(), gradient2)),
                new KeyFrame(Duration.seconds(16), new KeyValue(bgRect.fillProperty(), gradient1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        // Particles
        Pane particlePane = new Pane();
        createFloatingParticles(particlePane);

        // Load s
        try {
            shakeSound = new AudioClip(getClass().getResource("/sounds/shake.wav").toExternalForm());
            winSound = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());
            loseSound = new AudioClip(getClass().getResource("/sounds/lose.wav").toExternalForm());
            backsound = new AudioClip(getClass().getResource("/sounds/back.wav").toExternalForm());
            backsound.setCycleCount(AudioClip.INDEFINITE); // 🔁 Phát lặp vô hạn

        } catch (Exception e) {
            System.out.println("Could not load sound files");
        }

        // 4 player boxes - compact
        VBox userBox = buildCompactPlayerBox(player, "/avatars/avatar0.jpg", playerBalanceLabel, "bottom");
        VBox bot1Box = buildCompactPlayerBox(bot1, "/avatars/avatar1.jpg", bot1BalanceLabel, "left");
        VBox bot2Box = buildCompactPlayerBox(bot2, "/avatars/avatar2.jpg", bot2BalanceLabel, "top");
        VBox bot3Box = buildCompactPlayerBox(bot3, "/avatars/avatar3.jpg", bot3BalanceLabel, "right");

        // Title compact
        Label gameTitle = new Label("🎲 BẦU CUA TÔM CÁ 🎲");
        gameTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-padding: 8px;");

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#FFD700", 0.7));
        titleGlow.setRadius(15);
        gameTitle.setEffect(titleGlow);

        // Symbols grid - tối ưu kích thước
        GridPane symbolsGrid = new GridPane();
        symbolsGrid.setAlignment(Pos.CENTER);
        symbolsGrid.setHgap(15);
        symbolsGrid.setVgap(15);
        symbolsGrid.setPadding(new Insets(15));
        symbolsGrid.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 20px; " +
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-width: 2px; -fx-border-radius: 20px;");

        TextField[] betFields = new TextField[6];
        for (int i = 0; i < 6; i++) {
            VBox symbolCol = new VBox(6);
            symbolCol.setAlignment(Pos.CENTER);
            symbolCol.setPadding(new Insets(10));
            symbolCol.setPrefWidth(100);
            symbolCol.setPrefHeight(140);
            symbolCol.setStyle("-fx-background-color: rgba(255,255,255,0.15); " +
                    "-fx-background-radius: 15px; " +
                    "-fx-border-color: rgba(255,255,255,0.4); " +
                    "-fx-border-width: 1px; -fx-border-radius: 15px;");

            symbolCol.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), symbolCol);
                scale.setToX(1.03);
                scale.setToY(1.03);
                scale.play();

                DropShadow hover = new DropShadow();
                hover.setColor(Color.web("#FFD700", 0.6));
                hover.setRadius(10);
                symbolCol.setEffect(hover);
            });

            symbolCol.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), symbolCol);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
                symbolCol.setEffect(null);
            });

            ImageView img = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[i]), 60, 60, true, true));
            symbolsImageViews[i] = img;

            DropShadow imgGlow = new DropShadow();
            imgGlow.setColor(Color.web("#ffffff", 0.5));
            imgGlow.setRadius(8);
            img.setEffect(imgGlow);

            Label lbl = new Label(symbolNames[i]);
            lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white;");

            TextField betField = new TextField("0");
            betField.setMaxWidth(60);
            betField.setPrefHeight(28);
            betField.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                    "-fx-background-radius: 12px; " +
                    "-fx-border-color: #3498db; -fx-border-width: 1px; " +
                    "-fx-border-radius: 12px; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #2c3e50; -fx-alignment: center;");

            int idx = i;
            betField.textProperty().addListener((obs, oldV, newV) -> {
                try {
                    betAmount[idx] = Integer.parseInt(newV.isEmpty() ? "0" : newV);
                } catch (NumberFormatException e) {
                    betAmount[idx] = 0;
                }
            });
            betFields[i] = betField;
            symbolCol.getChildren().addAll(img, lbl, betField);

            int row = i < 3 ? 0 : 1;
            int col = i < 3 ? i : i - 3;
            symbolsGrid.add(symbolCol, col, row);
        }

        // Buttons
        Button betBtn = createStyledButton("🎯 ĐẶT CƯỢC", "#e74c3c", "#c0392b", 160, 40);
        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-padding: 10px 15px; " +
                "-fx-background-color: rgba(0,0,0,0.5); " +
                "-fx-background-radius: 15px; " +
                "-fx-text-alignment: center;");
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(500);

        // Dice box compact
        HBox diceBox = new HBox(10);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(10));
        diceBox.setPrefHeight(30);
        diceBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-background-radius: 20px; " +
                "-fx-border-color: rgba(255,255,255,0.4); " +
                "-fx-border-width: 2px; -fx-border-radius: 20px;");

        Button reloadBtn = createStyledButton("💰 NẠP LẠI", "#f39c12", "#e67e22", 120, 35);
        reloadBtn.setVisible(false);

        Button exitBtn = createStyledButton("⛔ THOÁT", "#bdc3c7", "#2c3e50", 120, 35);
        exitBtn.setOnAction(e -> {
            if (backgroundAnimation != null) backgroundAnimation.stop();
            if (backsound != null) backsound.stop(); // ⏹ Dừng nhạc nền khi rời phòng
            ProfileScene.showProfile(mainStage, user);
        });




        reloadBtn.setOnAction(e -> {
            player.setBalance(100000);
            playerBalanceLabel.setText(String.format("%,d", player.getBalance()));
            reloadBtn.setVisible(false);
            showMessage(resultLabel, "🎉 Đã nạp lại tiền! Chơi tiếp nào 😎", "#27ae60");
        });

        betBtn.setOnAction(e -> {
            int totalBet = Arrays.stream(betAmount).sum();
            if (totalBet <= 0) {
                showMessage(resultLabel, "⚠️ Chưa đặt cược!", "#f39c12");
                shakeAnimation(symbolsGrid);
                return;
            }
            if (totalBet > player.getBalance()) {
                showMessage(resultLabel, "❌ Không đủ tiền!", "#e74c3c");
                shakeAnimation(userBox);
                return;
            }

            player.setBalance(player.getBalance() - totalBet);
            playerBalanceLabel.setText(String.format("%,d", player.getBalance()));
            botsPlaceBet();

            bot1BalanceLabel.setText(String.format("%,d", bot1.getBalance()));
            bot2BalanceLabel.setText(String.format("%,d", bot2.getBalance()));
            bot3BalanceLabel.setText(String.format("%,d", bot3.getBalance()));

            Random rand = new Random();
            int[] finalDice = new int[3];
            for (int i = 0; i < 3; i++) finalDice[i] = rand.nextInt(6);

            betBtn.setDisable(true);
            betBtn.setText("🎲 ĐANG LẮC...");

            Timeline timeline = new Timeline();
            int shakeTimes = 30;
            for (int i = 0; i < shakeTimes; i++) {
                int finalI = i;
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(i * 70), ev -> {
                            if (finalI == 0 && shakeSound != null) shakeSound.play();
                            diceBox.getChildren().clear();
                            for (int j = 0; j < 3; j++) {
                                int diceIdx = rand.nextInt(6);
                                ImageView diceImg = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[diceIdx]), 45, 45, true, true));
                                diceImg.setRotate(rand.nextInt(360));

                                DropShadow diceGlow = new DropShadow();
                                diceGlow.setColor(Color.web("#FFD700", 0.7));
                                diceGlow.setRadius(12);
                                diceImg.setEffect(diceGlow);

                                diceBox.getChildren().add(diceImg);
                            }
                        })
                );
            }

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(shakeTimes * 70), ev -> {
                        diceBox.getChildren().clear();
                        for (int j = 0; j < 3; j++) {
                            ImageView diceImg = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[finalDice[j]]), 45, 45, true, true));

                            DropShadow resultGlow = new DropShadow();
                            resultGlow.setColor(Color.web("#00ff00", 0.8));
                            resultGlow.setRadius(20);
                            resultGlow.setSpread(0.4);
                            diceImg.setEffect(resultGlow);

                            diceBox.getChildren().add(diceImg);
                        }

                        // Hiệu ứng symbols trúngc
                        for (int d : finalDice) {
                            Glow symbolGlow = new Glow(0.8);
                            ColorAdjust bright = new ColorAdjust();
                            bright.setBrightness(0.2);
                            symbolGlow.setInput(bright);
                            symbolsImageViews[d].setEffect(symbolGlow);

                            ScaleTransition pulse = new ScaleTransition(Duration.millis(400), symbolsImageViews[d]);
                            pulse.setFromX(1.0);
                            pulse.setFromY(1.0);
                            pulse.setToX(1.2);
                            pulse.setToY(1.2);
                            pulse.setCycleCount(4);
                            pulse.setAutoReverse(true);
                            pulse.play();

                            Timeline clearEffect = new Timeline(
                                    new KeyFrame(Duration.seconds(2), ev2 -> symbolsImageViews[d].setEffect(null))
                            );
                            clearEffect.play();
                        }

                        // Tính kết quả
                        int win = 0;
                        for (int i = 0; i < 6; i++) {
                            int count = 0;
                            for (int d : finalDice) if (d == i) count++;
                            if (count > 0) {
                                win += betAmount[i] * count;
                                win += betAmount[i];
                            }
                        }
                        player.setBalance(player.getBalance() + win);

                        // Tính kết quả bot
                        for (int b = 0; b < bots.length; b++) {
                            Player bot = bots[b];
                            int botWin = 0;
                            for (int i = 0; i < 6; i++) {
                                int count = 0;
                                for (int d : finalDice) if (d == i) count++;
                                if (count > 0) {
                                    botWin += botBets[b][i] * count;
                                    botWin += botBets[b][i];
                                }
                            }
                            bot.setBalance(bot.getBalance() + botWin);
                        }

                        // Cập nhật UI
                        playerBalanceLabel.setText(String.format("%,d", player.getBalance()));
                        bot1BalanceLabel.setText(String.format("%,d", bot1.getBalance()));
                        bot2BalanceLabel.setText(String.format("%,d", bot2.getBalance()));
                        bot3BalanceLabel.setText(String.format("%,d", bot3.getBalance()));

                        // Âm thanh và thông báo
                        if (win > 0) {
                            if (winSound != null) winSound.play();
                            showMessage(resultLabel, "🎉 " + symbolNames[finalDice[0]] + ", "
                                    + symbolNames[finalDice[1]] + ", " + symbolNames[finalDice[2]]
                                    + " | Thắng " + String.format("%,d", win) + " xu! 🎊", "#27ae60");
                        } else {
                            if (loseSound != null) loseSound.play();
                            showMessage(resultLabel, "😢 " + symbolNames[finalDice[0]] + ", "
                                    + symbolNames[finalDice[1]] + ", " + symbolNames[finalDice[2]]
                                    + " | Không trúng. Thử lại! 💪", "#e74c3c");
                        }

                        if (player.getBalance() <= 200) {
                            reloadBtn.setVisible(true);
                        }

                        // Reset
                        Arrays.fill(betAmount, 0);
                        for (TextField f : betFields) f.setText("0");
                        for (int[] arr : botBets) Arrays.fill(arr, 0);

                        betBtn.setDisable(false);
                        betBtn.setText("🎯 ĐẶT CƯỢC");
                    })
            );
            timeline.play();
        });

        // Center area - compact layout ////////////////////////////////
        HBox buttonRow = new HBox(1, reloadBtn, exitBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(5, gameTitle, symbolsGrid, betBtn, diceBox, resultLabel, reloadBtn, exitBtn);
        centerBox.setAlignment(Pos.CENTER); // Căn giữa các thành phần
        centerBox.setPadding(new Insets(1));

        // Main layout - optimized for desktop
        BorderPane gameLayout = new BorderPane();
        gameLayout.setBottom(userBox);
        gameLayout.setLeft(bot1Box);
        gameLayout.setTop(bot2Box);
        gameLayout.setRight(bot3Box);

        ScrollPane scroll = new ScrollPane(centerBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        gameLayout.setCenter(scroll); // ✅ dùng scroll thay vì VBox trực tiếp


        BorderPane.setAlignment(userBox, Pos.CENTER);
        BorderPane.setAlignment(bot1Box, Pos.CENTER);
        BorderPane.setAlignment(bot2Box, Pos.CENTER);
        BorderPane.setAlignment(bot3Box, Pos.CENTER);

        BorderPane.setMargin(userBox, new Insets(10));
        BorderPane.setMargin(bot1Box, new Insets(10));
        BorderPane.setMargin(bot2Box, new Insets(10));
        BorderPane.setMargin(bot3Box, new Insets(10));

        root.getChildren().addAll(bgRect, particlePane, gameLayout);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("🎲 Bầu Cua - Bàn Chơi 4 Góc");
        stage.setScene(scene);
        stage.show();
        backsound.play(); // ▶ Bắt đầu phát nhạc nền

    }

    private void showMessage(Label msg, String text, String color) {
        msg.setText(text);
        msg.setStyle(msg.getStyle() + "-fx-background-color: " + color + ";");

        msg.setOpacity(0);
        msg.setScaleX(0.8);
        msg.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(Duration.millis(300), msg);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), msg);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition appear = new ParallelTransition(fade, scale);
        appear.play();
    }

    private void shakeAnimation(Region node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(40), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }


}