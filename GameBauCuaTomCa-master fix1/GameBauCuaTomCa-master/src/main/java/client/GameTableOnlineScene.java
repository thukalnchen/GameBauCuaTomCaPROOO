package client;

import common.User;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import com.google.gson.*;

import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;

import static java.lang.System.out;

public class GameTableOnlineScene implements MessageListener {
    private final String[] symbolNames = {"Nai", "Bầu", "Gà", "Cá", "Cua", "Tôm"};
    private final String[] symbolImg = {
            "/symbols/nai.png", "/symbols/bau.png", "/symbols/ga.png",
            "/symbols/ca.png", "/symbols/cua.png", "/symbols/tom.png"
    };

    private User user;
    private GameClientConnection connection;

    private int[] betAmount = new int[6];
    private TextField[] betFields = new TextField[6];
    private VBox[] playerBoxes = new VBox[4];
    private Label[] playerNameLabels = new Label[4];
    private Label[] playerBalanceLabels = new Label[4];
    private ImageView[] playerAvatarViews = new ImageView[4];

    private ImageView[] symbolsImageViews = new ImageView[6];
    private AudioClip shakeSound;
    private AudioClip winSound;
    private AudioClip loseSound;

    private HBox diceBox = new HBox(15);
    private Label resultLabel = new Label();

    private List<Map<String, Object>> playersInRoom = new ArrayList<>();

    public GameTableOnlineScene(User user) {
        this.user = user;
    }

    private void sendBetToServer() {
        int totalBet = Arrays.stream(betAmount).sum();
        if (totalBet <= 0) {
            resultLabel.setText("Bạn chưa đặt cược gì cả!");
            return;
        }
        JsonObject msg = new JsonObject();
        msg.addProperty("action", "PLACE_BET");
        msg.addProperty("username", user.getUsername());
        JsonArray arr = new JsonArray();
        for (int bet : betAmount) arr.add(bet);
        msg.add("bets", arr);
        connection.send(msg.toString());
    }

    private void sendJoinTable() {
        JsonObject msg = new JsonObject();
        msg.addProperty("action", "JOIN_TABLE");
        msg.addProperty("id", user.getId());
        msg.addProperty("username", user.getUsername());
        msg.addProperty("name", user.getName());
        msg.addProperty("avatar", user.getAvatar());
        msg.addProperty("balance", user.getBalance());
        connection.send(msg.toString());
    }

    public void start(Stage stage) {
        try {
            connection = new GameClientConnection("localhost", 8888, this);
        } catch (IOException e) {
            showAlert("Không kết nối được server: " + e.getMessage());
            return;
        }

        shakeSound = new AudioClip(getClass().getResource("/sounds/shake.wav").toExternalForm());
        winSound = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());
        loseSound = new AudioClip(getClass().getResource("/sounds/lose.wav").toExternalForm());

        Rectangle bgRect = new Rectangle(1600, 800);
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0f4c75")),
                new Stop(0.3, Color.web("#3282b8")),
                new Stop(0.7, Color.web("#bbe1fa")),
                new Stop(1, Color.web("#1e3c72")));
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.3, Color.web("#764ba2")),
                new Stop(0.7, Color.web("#f093fb")),
                new Stop(1, Color.web("#f5576c")));
        bgRect.setFill(gradient1);

        Timeline backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), gradient1)),
                new KeyFrame(Duration.seconds(8), new KeyValue(bgRect.fillProperty(), gradient2)),
                new KeyFrame(Duration.seconds(16), new KeyValue(bgRect.fillProperty(), gradient1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        Pane particlePane = new Pane();
        createFloatingParticles(particlePane);

        Label gameTitle = new Label("🎲 BẦU CUA TÔM CÁ 🎲");
        gameTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        gameTitle.setEffect(new DropShadow(15, Color.web("#FFD700", 0.7)));

        GridPane symbolsGrid = new GridPane();
        symbolsGrid.setAlignment(Pos.CENTER);
        symbolsGrid.setHgap(15);
        symbolsGrid.setVgap(15);
        symbolsGrid.setPadding(new Insets(15));
        symbolsGrid.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20px; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2px; -fx-border-radius: 20px;");

        for (int i = 0; i < 6; i++) {
            VBox symbolCol = new VBox(6);
            symbolCol.setAlignment(Pos.CENTER);
            symbolCol.setPrefWidth(100);
            symbolCol.setPrefHeight(140);
            symbolCol.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 15px; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 1px; -fx-border-radius: 15px;");

            int idx = i;
            symbolCol.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), symbolCol);
                scale.setToX(1.03);
                scale.setToY(1.03);
                scale.play();
                symbolCol.setEffect(new DropShadow(10, Color.web("#FFD700", 0.6)));
            });
            symbolCol.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), symbolCol);
                scale.setToX(1);
                scale.setToY(1);
                scale.play();
                symbolCol.setEffect(null);
            });

            ImageView img = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[i]), 60, 60, true, true));
            symbolsImageViews[i] = img;
            img.setEffect(new DropShadow(8, Color.web("#ffffff", 0.5)));

            Label lbl = new Label(symbolNames[i]);
            lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

            TextField betField = new TextField("0");
            betField.setMaxWidth(60);
            betField.setPrefHeight(28);
            betField.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 12px; -fx-border-color: #3498db; -fx-border-width: 1px; -fx-border-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");
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

        Button betBtn = createStyledButton("🎯 ĐẶT CƯỢC", "#e74c3c", "#c0392b", 160, 40);
        betBtn.setOnAction(e -> sendBetToServer());

        resultLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10px 15px; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15px;");

        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(10));
        diceBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 20px; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 2px; -fx-border-radius: 20px;");

        Button exitBtn = createStyledButton("🚪 Thoát", "#ff9ff3", "#f368e0", 180, 40);
        exitBtn.setOnAction(e -> {
            // Gửi LEAVE_TABLE lên server trước (tùy ý)
            JsonObject msg = new JsonObject();
            msg.addProperty("action", "LEAVE_TABLE");
            connection.send(msg.toString());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            // Đóng kết nối khỏi server
            connection.disconnect(); // Hoặc connection.close();

            // Chuyển về giao diện khác
            ProfileScene.showProfile(stage, user);
        });




        VBox centerBox = new VBox(5, gameTitle, symbolsGrid, betBtn, diceBox, resultLabel, exitBtn);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));

        for (int i = 0; i < 4; i++) {
            playerNameLabels[i] = new Label();
            playerBalanceLabels[i] = new Label();
            playerAvatarViews[i] = new ImageView();
            playerBoxes[i] = new VBox(5, playerAvatarViews[i], playerNameLabels[i], playerBalanceLabels[i]);
            playerBoxes[i].setAlignment(Pos.CENTER);
            playerBoxes[i].setStyle("-fx-padding: 6px; -fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 12px; -fx-border-color: white; -fx-border-width: 1px; -fx-border-radius: 12px;");
        }

        BorderPane gameLayout = new BorderPane();
        gameLayout.setBottom(playerBoxes[0]);
        gameLayout.setLeft(playerBoxes[1]);
        gameLayout.setTop(playerBoxes[2]);
        gameLayout.setRight(playerBoxes[3]);
        gameLayout.setCenter(centerBox);

        StackPane root = new StackPane();
        root.getChildren().addAll(bgRect, particlePane, gameLayout);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("🎲 Bầu Cua - Bàn Online");
        stage.setScene(scene);
        stage.show();

        sendJoinTable();
    }

    private Button createStyledButton(String text, String color1, String color2, double width, double height) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        button.setStyle("-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1px; -fx-border-radius: 20px;");
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color1, 0.5));
        glow.setRadius(10);
        button.setEffect(glow);
        return button;
    }

    private void createFloatingParticles(Pane pane) {
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            Circle particle = new Circle(random.nextDouble() * 2 + 1);
            particle.setFill(Color.web("#FFD700", random.nextDouble() * 0.5 + 0.2));
            particle.setLayoutX(random.nextDouble() * 1200);
            particle.setLayoutY(random.nextDouble() * 800);

            TranslateTransition move = new TranslateTransition(Duration.seconds(random.nextDouble() * 25 + 20), particle);
            move.setByX(random.nextDouble() * 300 - 150);
            move.setByY(random.nextDouble() * 300 - 150);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            FadeTransition fade = new FadeTransition(Duration.seconds(random.nextDouble() * 5 + 4), particle);
            fade.setFromValue(0.1);
            fade.setToValue(0.6);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            pane.getChildren().add(particle);
        }
    }
    public void sendToServer(String message) {
        out.println(message);
        out.flush();
    }


    // Xử lý message server trả về (được gọi từ thread mạng, luôn dùng Platform.runLater nếu cập nhật UI)
    @Override
    public void onServerMessage(String message) {
        Platform.runLater(() -> handleServerMessage(message));
    }

    @Override
    public void onDisconnect(Exception e) {
        Platform.runLater(() -> showAlert("Bạn đã thoát khỏi bàn thành công"));
    }

    private void handleServerMessage(String message) {
        // In ra log để debug mọi message từ server gửi về
        out.println("Message from server: [" + message + "]");
        // Kiểm tra message có phải là JSON object không trước khi parse
        if (message != null && message.trim().startsWith("{")) {
            JsonObject obj = new Gson().fromJson(message, JsonObject.class);
            String action = obj.get("action").getAsString();
            switch (action) {
                case "TABLE_UPDATE":
                    // Cập nhật thông tin player bàn (name/avatar/balance)
                    JsonArray arr = obj.getAsJsonArray("players");
                    playersInRoom.clear();
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject playerObj = arr.get(i).getAsJsonObject();
                        Map<String, Object> p = new HashMap<>();
                        p.put("name", playerObj.get("name").getAsString());
                        p.put("avatar", playerObj.get("avatar").getAsInt());
                        p.put("balance", playerObj.get("balance").getAsDouble());
                        playersInRoom.add(p);

                        // Update UI cho từng góc
                        playerNameLabels[i].setText("Tên: " + p.get("name"));
                        playerBalanceLabels[i].setText("Số dư: " + p.get("balance"));
                        String imgPath = "/avatars/avatar" + p.get("avatar") + ".jpg";
                        InputStream is = getClass().getResourceAsStream(imgPath);
                        if (is == null) imgPath = "/avatars/default.jpg";
                        playerAvatarViews[i].setImage(new Image(getClass().getResourceAsStream(imgPath), 48, 48, true, true));
                    }
                    for (int i = arr.size(); i < 4; i++) {
                        playerNameLabels[i].setText("Chờ người chơi vào bàn...");
                        playerBalanceLabels[i].setText("");
                        playerAvatarViews[i].setImage(null);
                    }
                    break;

                case "DICE_RESULT":
                    // Hiệu ứng lắc xúc xắc và âm thanh
                    int shakeTimes = 25; // lắc 25 lần
                    Random rand = new Random();

                    // Lưu dice thật để show sau hiệu ứng lắc
                    JsonArray diceArr = obj.getAsJsonArray("dice");
                    int[] finalDice = new int[diceArr.size()];
                    for (int i = 0; i < diceArr.size(); i++) {
                        finalDice[i] = diceArr.get(i).getAsInt();
                    }

                    Timeline timeline = new Timeline();
                    for (int i = 0; i < shakeTimes; i++) {
                        int finalI = i;
                        timeline.getKeyFrames().add(
                                new KeyFrame(Duration.millis(i * 50), ev -> {
                                    if (finalI == 0) shakeSound.play();
                                    diceBox.getChildren().clear();
                                    for (int j = 0; j < 3; j++) {
                                        int diceIdx = rand.nextInt(6);
                                        ImageView diceImg = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[diceIdx]), 48, 48, true, true));
                                        diceImg.setRotate(rand.nextInt(360));
                                        diceBox.getChildren().add(diceImg);
                                    }
                                })
                        );
                    }
                    timeline.getKeyFrames().add(
                            new KeyFrame(Duration.millis(shakeTimes * 50), ev -> {
                                diceBox.getChildren().clear();
                                for (int j = 0; j < 3; j++) {
                                    ImageView diceImg = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[finalDice[j]]), 48, 48, true, true));
                                    diceBox.getChildren().add(diceImg);
                                }

                                // Hiệu ứng Glow cho các symbol trúng
                                for (int d : finalDice) {
                                    symbolsImageViews[d].setEffect(new javafx.scene.effect.Glow(1));
                                    Timeline t = new Timeline(
                                            new KeyFrame(Duration.seconds(1.5), ev2 -> symbolsImageViews[d].setEffect(null))
                                    );
                                    t.play();
                                }

                                // Âm thanh win/lose (tính theo client)
                                resultLabel.setText(obj.get("resultText").getAsString());

                                JsonArray newBalances = obj.getAsJsonArray("balances");
                                double newBalance = newBalances.get(0).getAsDouble(); // (hoặc tìm đúng vị trí user)
                                if (newBalance > user.getBalance()) {
                                    winSound.play();
                                } else {
                                    loseSound.play();
                                }
                                user.setBalance((int)newBalance); // update balance object User nếu muốn

                                for (int i = 0; i < newBalances.size(); i++) {
                                    playerBalanceLabels[i].setText("Số dư: " + newBalances.get(i).getAsDouble());

                                }

                                for (TextField f : betFields) f.setText("0");
                                Arrays.fill(betAmount, 0);
                            })
                    );
                    timeline.play();
                    break;

                case "ERROR":
                    showAlert(obj.get("message").getAsString());
                    break;
            }
        } else {
            // Nếu không phải JSON object, log ra cảnh báo để dễ debug khi dev, không crash app
            System.err.println("Message KHÔNG PHẢI JSON object: [" + message + "]");
        }
    }


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
