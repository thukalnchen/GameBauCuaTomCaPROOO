package client;

import common.User;
import db.UserDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class AdRewardScene {

    public static void showAd(Stage ownerStage, User user, Runnable onRewarded) {
        Stage adStage = new Stage();
        adStage.initOwner(ownerStage);
        adStage.initModality(Modality.WINDOW_MODAL);
        adStage.setTitle("🎬 Quảng cáo");

        Label label = new Label("Đang hiển thị quảng cáo...");
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        Button rewardBtn = new Button("🎁 Nhận 10.000 xu");
        rewardBtn.setDisable(true);
        rewardBtn.setPrefWidth(200);
        rewardBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px;");

        // Danh sách các video
        String[] videoFiles = {
                "/videos/ad1_video.mp4",
                "/videos/ad_video.mp4",
                "/videos/ad2_video.mp4"
        };

        // Random chọn 1 video
        Random random = new Random();
        String selectedVideo = videoFiles[random.nextInt(videoFiles.length)];

        // Load video
        URL resource = AdRewardScene.class.getResource(selectedVideo);
        if (resource == null) {
            System.err.println("Không tìm thấy video: " + selectedVideo);
            return;
        }

        Media media = new Media(resource.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setVolume(0.7); // tuỳ chọn

        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.setFitWidth(300);
        mediaView.setPreserveRatio(true);

        // Đếm ngược (giả lập 20 giây hoặc hết video)
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            rewardBtn.setDisable(false);
            label.setText("🎉 Bạn đã xem xong! Nhấn để nhận thưởng.");
        }));
        timeline.play();

        rewardBtn.setOnAction(e -> {
            int newBalance = user.getBalance() + 10000;
            user.setBalance(newBalance);

            boolean saved = UserDAO.updateBalance(user.getId(), newBalance);
            if (!saved) {
                System.out.println("❌ Không thể lưu tiền vào DB!");
            }

            if (onRewarded != null) onRewarded.run();
            adStage.close();
        });

        VBox box = new VBox(15, label, mediaView, rewardBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #74ebd5, #acb6e5);");

        adStage.setScene(new Scene(box, 400, 400));
        adStage.show();
    }
}
