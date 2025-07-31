package common;

import java.sql.Timestamp;

public class NapTienLog {
    private int id;
    private int userId;
    private int amount;
    private String paymentId;
    private String payerId;
    private Timestamp createdAt;

    public NapTienLog(int id, int userId, int amount, String paymentId, String payerId, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.paymentId = paymentId;
        this.payerId = payerId;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getAmount() { return amount; }
    public String getPaymentId() { return paymentId; }
    public String getPayerId() { return payerId; }
    public Timestamp getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "NapTienLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", paymentId='" + paymentId + '\'' +
                ", payerId='" + payerId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}