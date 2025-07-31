package client;

import common.User;
import db.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import common.NapTienLog;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class LichSuNapTienView {

    private User currentUser;

    public LichSuNapTienView() {
        // Constructor mặc định
    }

    public void start(User user, Stage parentStage) {
        this.currentUser = user;

        // Tạo Stage mới cho cửa sổ lịch sử
        Stage stage = new Stage();

        // Tiêu đề
        Label titleLabel = new Label("Lịch sử nạp tiền - " + user.getUsername());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TableView<NapTienLog> table = new TableView<>();
        table.setPrefHeight(400);

        // Cột ID
        TableColumn<NapTienLog, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idCol.setMinWidth(50);

        // Cột Số tiền (format với dấu phẩy)
        TableColumn<NapTienLog, String> amountCol = new TableColumn<>("Số tiền (VND)");
        amountCol.setCellValueFactory(cellData -> {
            DecimalFormat df = new DecimalFormat("#,###");
            return new SimpleStringProperty(df.format(cellData.getValue().getAmount()));
        });
        amountCol.setMinWidth(120);

        // Cột Payment ID
        TableColumn<NapTienLog, String> paymentIdCol = new TableColumn<>("Mã giao dịch");
        paymentIdCol.setCellValueFactory(cellData -> {
            String paymentId = cellData.getValue().getPaymentId();
            return new SimpleStringProperty(paymentId != null ? paymentId : "N/A");
        });
        paymentIdCol.setMinWidth(200);

        // Cột Payer ID
        TableColumn<NapTienLog, String> payerIdCol = new TableColumn<>("Mã người thanh toán");
        payerIdCol.setCellValueFactory(cellData -> {
            String payerId = cellData.getValue().getPayerId();
            return new SimpleStringProperty(payerId != null ? payerId : "N/A");
        });
        payerIdCol.setMinWidth(150);

        // Cột Thời gian
        TableColumn<NapTienLog, String> timeCol = new TableColumn<>("Thời gian");
        timeCol.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getCreatedAt()));
        });
        timeCol.setMinWidth(150);

        table.getColumns().addAll(idCol, amountCol, paymentIdCol, payerIdCol, timeCol);

        // Lấy dữ liệu từ database
        List<NapTienLog> logs = UserDAO.getLichSuNapTien(currentUser.getId());
        ObservableList<NapTienLog> data = FXCollections.observableArrayList(logs);
        table.setItems(data);

        // Thông tin tổng kết
        Label totalLabel = new Label();
        if (!logs.isEmpty()) {
            int totalAmount = logs.stream().mapToInt(NapTienLog::getAmount).sum();
            DecimalFormat df = new DecimalFormat("#,###");
            totalLabel.setText("Tổng số lần nạp: " + logs.size() + " | Tổng số tiền: " + df.format(totalAmount) + " VND");
        } else {
            totalLabel.setText("Chưa có lịch sử nạp tiền");
        }
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(titleLabel, table, totalLabel);

        Scene scene = new Scene(root, 800, 500);

        stage.setTitle("Lịch sử nạp tiền");
        stage.setScene(scene);
        stage.show();
    }
}