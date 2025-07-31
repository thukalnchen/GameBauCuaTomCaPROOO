package payment;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import java.util.ArrayList;
import java.util.List;

import static com.paypal.base.Constants.*;

public class PayPalService {

    /**
     * Tạo thanh toán với PayPal
     *
     * @param total      Tổng số tiền (VD: 5.0)
     * @param currency   Loại tiền tệ (VD: "USD")
     * @param method     Phương thức thanh toán ("paypal")
     * @param intent     Ý định thanh toán ("sale")
     * @param description Mô tả đơn hàng
     * @param cancelUrl  URL khi người dùng hủy thanh toán
     * @param successUrl URL khi người dùng thanh toán thành công
     * @return URL PayPal để redirect người dùng đến
     * @throws PayPalRESTException nếu có lỗi xảy ra khi gọi API
     */
    public String createPayment(Double total, String currency, String method,
                                String intent, String description,
                                String cancelUrl, String successUrl) throws PayPalRESTException {

        // Format tiền về dạng chuỗi có 2 chữ số thập phân
        String formattedTotal = String.format("%.2f", total);

        Amount amount = new Amount();
        amount.setCurrency(currency); // nên dùng biến currency đã truyền vào
        amount.setTotal(formattedTotal); // ✅ Gán total trước khi gọi getTotal

        System.out.println("Đã tạo giao dịch với số tiền " + amount.getTotal() + " " + amount.getCurrency());



        // Tạo transaction
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Cấu hình người thanh toán
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toLowerCase());

        // Tạo Payment
        Payment payment = new Payment();
        payment.setIntent(intent.toLowerCase());
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Thiết lập URL redirect
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // Gọi API PayPal
        APIContext apiContext = PayPalConfig.getAPIContext();
        Payment createdPayment = payment.create(apiContext);

        // Tìm link redirect để trả về
        for (Links link : createdPayment.getLinks()) {
            if ("approval_url".equalsIgnoreCase(link.getRel())) {
                return link.getHref();
            }
        }

        return null;
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        APIContext apiContext = PayPalConfig.getAPIContext();

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecute);
    }
}
