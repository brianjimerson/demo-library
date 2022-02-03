package dev.snbv2.payments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private static final Log LOG = LogFactory.getLog(PaymentController.class);

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentMessageService paymentMessageService;

    @PostMapping("/payment")
    public String processPayment(@RequestBody Payment payment) {

        String result = null;
        try {
            result = paymentService.processPayment(payment);
            paymentMessageService.sendMessage(payment);
        } catch (Exception e) {
            LOG.error("Error processing payment", e);
            result = e.getMessage();
        }

        return result;
    }
}
