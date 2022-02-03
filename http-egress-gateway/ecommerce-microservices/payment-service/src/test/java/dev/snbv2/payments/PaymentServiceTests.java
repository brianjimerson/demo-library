package dev.snbv2.payments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PaymentServiceTests {

    private static final Log LOG = LogFactory.getLog(PaymentServiceTests.class);
    @Autowired
    PaymentService paymentService;

    @Test
    public void testSuccessfulCard() {

        Payment payment = new Payment("5555555555554444", "111", 12, 25, 102.25, "usd", "Test payment");

        String result = null;
        try {
            result = paymentService.processPayment(payment);
        } catch (Exception e) {
            Assertions.fail("Error processing payment", e);
        }
        LOG.info(String.format("Payment result = [%s].", result));
        Assertions.assertEquals("succeeded", result);
    }

    @Test
    public void testCardDeclined() {

        Payment payment = new Payment("4000000000000002", "111", 12, 25, 102.25, "usd", "Test payment");
        Assertions.assertThrows(Exception.class, () -> {paymentService.processPayment(payment);});
    }
}
