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
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import com.google.gson.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import static java.lang.System.out;

public class GameTableOnlineScene implements MessageListener {

    private Stage mainStage;
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
    private AudioClip backSound;
    private HBox diceBox = new HBox(15);
    private Label resultLabel = new Label();
    private List<Map<String, Object>> playersInRoom = new ArrayList<>();
    private Label dealerLabel = new Label();
    private Button betBtn; // Để thao tác dễ hơn
    private boolean isDealer = false; // Lưu trạng thái mình có phải dealer không




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
        this.mainStage = stage;
        try {
            connection = new GameClientConnection("localhost", 8888, this);
        } catch (IOException e) {
            showAlert("Không kết nối được server: " + e.getMessage());
            return;
        }

        shakeSound = new AudioClip(getClass().getResource("/sounds/shake.wav").toExternalForm());
        winSound = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());
        loseSound = new AudioClip(getClass().getResource("/sounds/lose.wav").toExternalForm());
        backSound = new AudioClip(getClass().getResource("/sounds/back.wav").toExternalForm());
        backSound.setCycleCount(AudioClip.INDEFINITE);

        // Tạo background với nhiều lớp gradient động
        Rectangle bgRect = new Rectangle(1600, 800);

        // Gradient 1 - Màu xanh dương đậm
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0f4c75")),
                new Stop(0.2, Color.web("#3282b8")),
                new Stop(0.4, Color.web("#bbe1fa")),
                new Stop(0.6, Color.web("#1e3c72")),
                new Stop(0.8, Color.web("#2a5298")),
                new Stop(1, Color.web("#0f4c75")));

        // Gradient 2 - Màu tím hồng
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")),
                new Stop(0.2, Color.web("#764ba2")),
                new Stop(0.4, Color.web("#f093fb")),
                new Stop(0.6, Color.web("#f5576c")),
                new Stop(0.8, Color.web("#4facfe")),
                new Stop(1, Color.web("#00f2fe")));

        // Gradient 3 - Màu vàng cam
        LinearGradient gradient3 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff9a9e")),
                new Stop(0.2, Color.web("#fecfef")),
                new Stop(0.4, Color.web("#fecfef")),
                new Stop(0.6, Color.web("#ff9a9e")),
                new Stop(0.8, Color.web("#a8edea")),
                new Stop(1, Color.web("#fed6e3")));

        bgRect.setFill(gradient1);

        // Animation chuyển đổi gradient phức tạp hơn
        Timeline backgroundAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgRect.fillProperty(), gradient1)),
                new KeyFrame(Duration.seconds(6), new KeyValue(bgRect.fillProperty(), gradient2)),
                new KeyFrame(Duration.seconds(12), new KeyValue(bgRect.fillProperty(), gradient3)),
                new KeyFrame(Duration.seconds(18), new KeyValue(bgRect.fillProperty(), gradient1))
        );
        backgroundAnimation.setCycleCount(Timeline.INDEFINITE);
        backgroundAnimation.play();

        // Tạo pane cho các hiệu ứng nền
        Pane effectPane = new Pane();
        createFloatingSymbols(effectPane);
        createFloatingParticles(effectPane);
        createPulsingCircles(effectPane);

        Label gameTitle = new Label("🎲 BẦU CUA TÔM CÁ 🎲");
        gameTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Hiệu ứng glow cho title
        DropShadow titleGlow = new DropShadow(20, Color.web("#FFD700", 0.8));
        titleGlow.setInput(new Glow(0.5));
        gameTitle.setEffect(titleGlow);

        // Animation cho title
        ScaleTransition titleScale = new ScaleTransition(Duration.seconds(2), gameTitle);
        titleScale.setFromX(1.0);
        titleScale.setFromY(1.0);
        titleScale.setToX(1.05);
        titleScale.setToY(1.05);
        titleScale.setCycleCount(Timeline.INDEFINITE);
        titleScale.setAutoReverse(true);
        titleScale.play();

        GridPane symbolsGrid = new GridPane();
        symbolsGrid.setAlignment(Pos.CENTER);
        symbolsGrid.setHgap(15);
        symbolsGrid.setVgap(15);
        symbolsGrid.setPadding(new Insets(15));
        symbolsGrid.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 25px; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 2px; -fx-border-radius: 25px;");

        // Hiệu ứng glow cho grid
        DropShadow gridGlow = new DropShadow(15, Color.web("#00FFFF", 0.3));
        symbolsGrid.setEffect(gridGlow);

        for (int i = 0; i < 6; i++) {
            VBox symbolCol = new VBox(6);
            symbolCol.setAlignment(Pos.CENTER);
            symbolCol.setPrefWidth(100);
            symbolCol.setPrefHeight(140);
            symbolCol.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 18px; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 1px; -fx-border-radius: 18px;");

            int idx = i;
            symbolCol.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), symbolCol);
                scale.setToX(1.08);
                scale.setToY(1.08);
                scale.play();

                DropShadow hoverGlow = new DropShadow(15, Color.web("#FFD700", 0.8));
                hoverGlow.setInput(new Glow(0.7));
                symbolCol.setEffect(hoverGlow);
            });

            symbolCol.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), symbolCol);
                scale.setToX(1);
                scale.setToY(1);
                scale.play();
                symbolCol.setEffect(null);
            });

            ImageView img = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[i]), 60, 60, true, true));
            symbolsImageViews[i] = img;

            // Hiệu ứng xoay nhẹ cho symbol
            RotateTransition rotate = new RotateTransition(Duration.seconds(8 + i), img);
            rotate.setByAngle(360);
            rotate.setCycleCount(Timeline.INDEFINITE);
            rotate.play();

            DropShadow imgShadow = new DropShadow(10, Color.web("#ffffff", 0.6));
            img.setEffect(imgShadow);

            Label lbl = new Label(symbolNames[i]);
            lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");

            TextField betField = new TextField("0");
            betField.setMaxWidth(60);
            betField.setPrefHeight(28);
            betField.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 15px; -fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 15px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");

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

        betBtn = createStyledButton("🎯 ĐẶT CƯỢC", "#e74c3c", "#c0392b", 160, 40);
        betBtn.setOnAction(e -> sendBetToServer());


        resultLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 12px 18px; -fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 18px; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1px; -fx-border-radius: 18px;");

        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(15));
        diceBox.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 25px; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2px; -fx-border-radius: 25px;");

        // Hiệu ứng glow cho dice box
        DropShadow diceGlow = new DropShadow(12, Color.web("#FF6B6B", 0.4));
        diceBox.setEffect(diceGlow);

        Button exitBtn = createStyledButton("🚪 Thoát", "#ff9ff3", "#f368e0", 180, 40);
        exitBtn.setOnAction(e -> {
            JsonObject msg = new JsonObject();
            msg.addProperty("action", "LEAVE_TABLE");
            connection.send(msg.toString());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            connection.disconnect();
            ProfileScene.showProfile(stage, user);
        });

        VBox centerBox = new VBox(8, gameTitle, dealerLabel, symbolsGrid, betBtn, diceBox, resultLabel, exitBtn);

        // thêm xì tai cho glow cho dealerLabel =))))
        dealerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-padding: 10px;");
        dealerLabel.setAlignment(Pos.CENTER);

        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(15));

        for (int i = 0; i < 4; i++) {
            playerNameLabels[i] = new Label();
            playerBalanceLabels[i] = new Label();
            playerAvatarViews[i] = new ImageView();
            playerBoxes[i] = new VBox(5, playerAvatarViews[i], playerNameLabels[i], playerBalanceLabels[i]);
            playerBoxes[i].setAlignment(Pos.CENTER);
            playerBoxes[i].setStyle("-fx-padding: 8px; -fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 15px; -fx-border-color: rgba(255,255,255,0.6); -fx-border-width: 1px; -fx-border-radius: 15px;");

            // Hiệu ứng glow nhẹ cho player boxes
            DropShadow playerGlow = new DropShadow(8, Color.web("#4ECDC4", 0.3));
            playerBoxes[i].setEffect(playerGlow);
        }

        BorderPane gameLayout = new BorderPane();
        gameLayout.setBottom(playerBoxes[0]);
        gameLayout.setLeft(playerBoxes[1]);
        gameLayout.setTop(playerBoxes[2]);
        gameLayout.setRight(playerBoxes[3]);
        gameLayout.setCenter(centerBox);

        StackPane root = new StackPane();
        root.getChildren().addAll(bgRect, effectPane, gameLayout);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("🎲 Bầu Cua - Bàn Online");
        stage.setScene(scene);
        stage.show();

        sendJoinTable();
        backSound.play();
    }


    private Button createStyledButton(String text, String color1, String color2, double width, double height) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        button.setStyle("-fx-background-color: linear-gradient(to right, " + color1 + ", " + color2 + "); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 22px; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 1px; -fx-border-radius: 22px;");

        DropShadow glow = new DropShadow(12, Color.web(color1, 0.6));
        button.setEffect(glow);

        // Hiệu ứng hover cho button
        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            DropShadow hoverGlow = new DropShadow(15, Color.web(color1, 0.8));
            hoverGlow.setInput(new Glow(0.3));
            button.setEffect(hoverGlow);
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            button.setEffect(glow);
        });

        return button;
    }

    // Tạo các biểu tượng bầu cua bay qua bay lại
    private void createFloatingSymbols(Pane pane) {
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            int symbolIndex = random.nextInt(6);
            ImageView floatingSymbol = new ImageView(new Image(getClass().getResourceAsStream(symbolImg[symbolIndex]), 40, 40, true, true));

            floatingSymbol.setOpacity(0.15 + random.nextDouble() * 0.25);
            floatingSymbol.setLayoutX(random.nextDouble() * 1200);
            floatingSymbol.setLayoutY(random.nextDouble() * 800);

            // Hiệu ứng di chuyển ngang
            TranslateTransition moveX = new TranslateTransition(Duration.seconds(15 + random.nextDouble() * 20), floatingSymbol);
            moveX.setFromX(-100);
            moveX.setToX(1300);
            moveX.setCycleCount(Timeline.INDEFINITE);
            moveX.play();

            // Hiệu ứng di chuyển dọc (lên xuống)
            TranslateTransition moveY = new TranslateTransition(Duration.seconds(8 + random.nextDouble() * 12), floatingSymbol);
            moveY.setByY(random.nextDouble() * 200 - 100);
            moveY.setCycleCount(Timeline.INDEFINITE);
            moveY.setAutoReverse(true);
            moveY.play();

            // Hiệu ứng xoay
            RotateTransition rotate = new RotateTransition(Duration.seconds(10 + random.nextDouble() * 15), floatingSymbol);
            rotate.setByAngle(360);
            rotate.setCycleCount(Timeline.INDEFINITE);
            rotate.play();

            // Hiệu ứng fade
            FadeTransition fade = new FadeTransition(Duration.seconds(3 + random.nextDouble() * 4), floatingSymbol);
            fade.setFromValue(0.1);
            fade.setToValue(0.4);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            // Hiệu ứng scale
            ScaleTransition scale = new ScaleTransition(Duration.seconds(6 + random.nextDouble() * 8), floatingSymbol);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.2);
            scale.setToY(1.2);
            scale.setCycleCount(Timeline.INDEFINITE);
            scale.setAutoReverse(true);
            scale.play();

            pane.getChildren().add(floatingSymbol);
        }
    }

    // Tạo các hạt sáng bay
    private void createFloatingParticles(Pane pane) {
        Random random = new Random();

        for (int i = 0; i < 30; i++) {
            Circle particle = new Circle(random.nextDouble() * 3 + 1);

            // Màu sắc đa dạng
            Color[] colors = {
                    Color.web("#FFD700"), Color.web("#FF6B6B"), Color.web("#4ECDC4"),
                    Color.web("#45B7D1"), Color.web("#96CEB4"), Color.web("#FFEAA7"),
                    Color.web("#DDA0DD"), Color.web("#98D8C8")
            };

            particle.setFill(colors[random.nextInt(colors.length)].deriveColor(0, 1, 1, random.nextDouble() * 0.6 + 0.2));
            particle.setLayoutX(random.nextDouble() * 1200);
            particle.setLayoutY(random.nextDouble() * 800);

            // Di chuyển theo đường cong
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.setX(random.nextDouble() * 1200);
            moveTo.setY(random.nextDouble() * 800);

            CubicCurveTo curveTo = new CubicCurveTo();
            curveTo.setControlX1(random.nextDouble() * 1200);
            curveTo.setControlY1(random.nextDouble() * 800);
            curveTo.setControlX2(random.nextDouble() * 1200);
            curveTo.setControlY2(random.nextDouble() * 800);
            curveTo.setX(random.nextDouble() * 1200);
            curveTo.setY(random.nextDouble() * 800);

            path.getElements().add(moveTo);
            path.getElements().add(curveTo);

            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(20 + random.nextDouble() * 30));
            pathTransition.setPath(path);
            pathTransition.setNode(particle);
            pathTransition.setCycleCount(Timeline.INDEFINITE);
            pathTransition.play();

            // Hiệu ứng fade
            FadeTransition fade = new FadeTransition(Duration.seconds(2 + random.nextDouble() * 4), particle);
            fade.setFromValue(0.1);
            fade.setToValue(0.8);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            pane.getChildren().add(particle);
        }
    }

    // Tạo các vòng tròn phát sáng
    private void createPulsingCircles(Pane pane) {
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            Circle circle = new Circle(random.nextDouble() * 50 + 20);
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.web("#FFD700", 0.3));
            circle.setStrokeWidth(2);
            circle.setLayoutX(random.nextDouble() * 1200);
            circle.setLayoutY(random.nextDouble() * 800);

            // Hiệu ứng phóng to thu nhỏ
            ScaleTransition scale = new ScaleTransition(Duration.seconds(4 + random.nextDouble() * 6), circle);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(2.0);
            scale.setToY(2.0);
            scale.setCycleCount(Timeline.INDEFINITE);
            scale.setAutoReverse(true);
            scale.play();

            // Hiệu ứng fade
            FadeTransition fade = new FadeTransition(Duration.seconds(3 + random.nextDouble() * 4), circle);
            fade.setFromValue(0.1);
            fade.setToValue(0.5);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            // Di chuyển chậm
            TranslateTransition move = new TranslateTransition(Duration.seconds(25 + random.nextDouble() * 35), circle);
            move.setByX(random.nextDouble() * 400 - 200);
            move.setByY(random.nextDouble() * 400 - 200);
            move.setCycleCount(Timeline.INDEFINITE);
            move.setAutoReverse(true);
            move.play();

            pane.getChildren().add(circle);
        }
    }

    public void sendToServer(String message) {
        out.println(message);
        out.flush();
    }

    @Override
    public void onServerMessage(String message) {
        Platform.runLater(() -> handleServerMessage(message));
    }

    @Override
    public void onDisconnect(Exception e) {
        Platform.runLater(() -> showAlert("Bạn đã thoát khỏi bàn thành công"));
    }



    private void handleServerMessage(String message) {
        out.println("Message from server: [" + message + "]");

        if (message != null && message.trim().startsWith("{")) {
            JsonObject obj = new Gson().fromJson(message, JsonObject.class);
            String action = obj.get("action").getAsString();

            switch (action) {
                case "TABLE_UPDATE":
                    JsonArray arr = obj.getAsJsonArray("players");
                    playersInRoom.clear();

                    // Đọc dealerId và dealerName từ server
                    int dealerId = obj.has("dealerId") ? obj.get("dealerId").getAsInt() : -1;
                    String dealerName = obj.has("dealerName") ? obj.get("dealerName").getAsString() : "";

                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject playerObj = arr.get(i).getAsJsonObject();
                        Map<String, Object> p = new HashMap<>();
                        p.put("name", playerObj.get("name").getAsString());
                        p.put("avatar", playerObj.get("avatar").getAsInt());
                        p.put("balance", playerObj.get("balance").getAsDouble());
                        int thisPlayerId = playerObj.has("userId") ? playerObj.get("userId").getAsInt() : -1;
                        p.put("userId", thisPlayerId);
                        playersInRoom.add(p);

                        // Hiển thị rõ người làm cái trên UI
                        if (thisPlayerId == dealerId) {
                            playerNameLabels[i].setText("Tên: " + p.get("name") + " (CÁI)");
                            playerNameLabels[i].setTextFill(Color.web("#FFD700"));
                            playerAvatarViews[i].setEffect(new DropShadow(22, Color.web("#FFD700", 0.8)));
                        } else {
                            playerNameLabels[i].setText("Tên: " + p.get("name"));
                            playerNameLabels[i].setTextFill(Color.WHITE);
                            playerAvatarViews[i].setEffect(null);
                        }
                        String imgPath = "/avatars/avatar" + p.get("avatar") + ".jpg";
                        InputStream is = getClass().getResourceAsStream(imgPath);
                        if (is == null) imgPath = "/avatars/default.jpg";
                        playerAvatarViews[i].setImage(new Image(getClass().getResourceAsStream(imgPath), 48, 48, true, true));
                        playerBalanceLabels[i].setText("Số dư: " + p.get("balance"));
                    }
                    for (int i = arr.size(); i < 4; i++) {
                        playerNameLabels[i].setText("Chờ người chơi vào bàn...");
                        playerNameLabels[i].setTextFill(Color.WHITE);
                        playerBalanceLabels[i].setText("");
                        playerAvatarViews[i].setEffect(null);
                        playerAvatarViews[i].setImage(null);
                    }

                    // Cập nhật label dealer nổi bật
                    if (dealerId == user.getId()) {
                        dealerLabel.setText("🌟 Bạn đang làm CÁI (Dealer) 🌟");
                        dealerLabel.setTextFill(Color.web("#FFD700"));
                    } else if (!dealerName.isEmpty() && dealerId != -1) {
                        dealerLabel.setText("🃏 " + dealerName + " đang làm CÁI (Dealer)");
                        dealerLabel.setTextFill(Color.web("#FFD700"));
                    } else {
                        dealerLabel.setText("Chưa có ai làm cái");
                        dealerLabel.setTextFill(Color.WHITE);
                    }

                    if (dealerId == user.getId()) {
                        for (TextField f : betFields) f.setDisable(true); // Dealer không được đặt cược
                    } else {
                        for (TextField f : betFields) f.setDisable(false); // Các con được đặt cược bình thường
                    }

                    isDealer = (dealerId == user.getId());
                    if (isDealer) {
                        betBtn.setText("🎲 LẮC XÚC XẮC");
                        betBtn.setDisable(false); // dealer luôn được bấm nút này
                        betBtn.setOnAction(ev -> {
                            JsonObject msg = new JsonObject();
                            msg.addProperty("action", "ROLL_DICE");
                            connection.send(msg.toString());
                        });
                        // Disable đặt cược
                        for (TextField f : betFields) f.setDisable(true);
                    } else {
                        betBtn.setText("🎯 ĐẶT CƯỢC");
                        betBtn.setOnAction(ev -> sendBetToServer());
                        // Con chỉ được đặt cược nếu chưa gửi cược
                        for (TextField f : betFields) f.setDisable(false);
                    }

                    break;


                case "DICE_RESULT":
                    int shakeTimes = 25;
                    Random rand = new Random();

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

                                // Hiệu ứng Glow mạnh hơn cho các symbol trúng
                                for (int d : finalDice) {
                                    Glow glow = new Glow(1.0);
                                    DropShadow winGlow = new DropShadow(20, Color.web("#FFD700", 0.9));
                                    winGlow.setInput(glow);
                                    symbolsImageViews[d].setEffect(winGlow);

                                    Timeline glowTimeline = new Timeline(
                                            new KeyFrame(Duration.seconds(2.5), ev2 -> symbolsImageViews[d].setEffect(null))
                                    );
                                    glowTimeline.play();
                                }

                                resultLabel.setText(obj.get("resultText").getAsString());
                                JsonArray newBalances = obj.getAsJsonArray("balances");
                                double newBalance = newBalances.get(0).getAsDouble();

                                if (newBalance > user.getBalance()) {
                                    winSound.play();
                                } else {
                                    loseSound.play();
                                }

                                user.setBalance((int)newBalance);
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

                case "KICKED":
                    String reason = obj.has("message") ? obj.get("message").getAsString() : "Bạn đã bị đá khỏi bàn!";
                    double kickedBalance = obj.has("balance") ? obj.get("balance").getAsDouble() : user.getBalance();
                    
                    // Cập nhật balance của user
                    user.setBalance((int)kickedBalance);
                    
                    // Hiển thị thông báo kick với hiệu ứng
                    Label kickLabel = new Label(reason);
                    kickLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF6B6B; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20px; -fx-background-radius: 10px;");
                    kickLabel.setAlignment(Pos.CENTER);
                    kickLabel.setMinWidth(400);
                    kickLabel.setMaxWidth(400);
                    
                    StackPane kickPane = new StackPane(kickLabel);
                    kickPane.setAlignment(Pos.CENTER);
                    
                    // Thêm hiệu ứng vào scene
                    if (mainStage.getScene().getRoot() instanceof StackPane) {
                        StackPane root = (StackPane) mainStage.getScene().getRoot();
                        root.getChildren().add(kickPane);
                    }
                    
                    // Animation cho thông báo
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), kickLabel);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), kickLabel);
                    scaleUp.setFromX(0.5);
                    scaleUp.setFromY(0.5);
                    scaleUp.setToX(1.0);
                    scaleUp.setToY(1.0);
                    
                    ParallelTransition showKick = new ParallelTransition(fadeIn, scaleUp);
                    
                    // Sau khi hiển thị thông báo, delay rồi chuyển về profile
                    showKick.setOnFinished(e -> {
                        // Ngắt kết nối sau khi hoàn thành animation
                        PauseTransition delay = new PauseTransition(Duration.seconds(2));
                        delay.setOnFinished(ev -> {
                            connection.disconnect();
                            showProfileAndAlert(reason);
                        });
                        delay.play();
                    });
                    
                    showKick.play();
                    break;

            }
        } else {
            System.err.println("Message KHÔNG PHẢI JSON object: [" + message + "]");
        }
    }

    private void showProfileAndAlert(String msg) {
        ProfileScene.showProfile(mainStage, user);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }



    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
