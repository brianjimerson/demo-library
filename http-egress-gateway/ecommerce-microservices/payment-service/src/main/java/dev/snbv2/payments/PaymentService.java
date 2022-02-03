package dev.snbv2.payments;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import com.stripe.param.ChargeCreateParams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Log LOG = LogFactory.getLog(PaymentService.class);

    @Value("${stripe.api.api-key}")
    String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String processPayment(Payment payment) throws Exception {
        Stripe.apiKey = this.getApiKey();

        Map<String, Object> card = new HashMap<>();
        card.put("cvc", payment.getCvc());
        card.put("number", payment.getCardNumber());
        card.put("exp_month", payment.getExpirationMonth());
        card.put("exp_year", payment.getExpirationYear());

        Map<String, Object> cardParams = new HashMap<>();
        cardParams.put("card", card);
        Token token = null;
        try {
            token = Token.create(cardParams);
        } catch (StripeException e) {
            LOG.error("Error creating token.", e);
            throw new Exception(e);
        }

        ChargeCreateParams chargeParams = 
            ChargeCreateParams.builder()
                .setAmount(Math.round(payment.getAmount() * 100))
                .setCurrency(payment.getCurrency())
                .setSource(token.getId())
                .build();
        Charge charge = null;
        try {
            charge = Charge.create(chargeParams);
        } catch (StripeException e) {
            LOG.error("Error creating charge.", e);
            throw new Exception(e);
        }
        LOG.info(String.format("Charge create result = [%s]", charge.getStatus()));

        return charge.getStatus();
    }
    
}
