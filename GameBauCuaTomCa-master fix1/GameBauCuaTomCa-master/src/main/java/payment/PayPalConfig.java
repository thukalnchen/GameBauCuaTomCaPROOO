package payment;


import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import java.util.HashMap;
import java.util.Map;

public class PayPalConfig {
    private static final String CLIENT_ID = "AROmayC925o4t-1MyQ9YlPVVfcq5TXBRkDj4qBDrkN6TS1tFKjEFxFkN_-no0X6y_1fCnDqxmvyPB0cY";
    private static final String CLIENT_SECRET = "EFgQKpAnAmR-82fPQMGaZDfruOB2h2ecOTJAw17OPU3CKrH2dCBrVWAxHkWjnWUA73X8e6H5IokgJLka";
    private static final String MODE = "sandbox"; // Để test, dùng "sandbox"; khi triển khai thực tế dùng "live"

    public static APIContext getAPIContext() {
        APIContext context = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
        return context;
    }
}