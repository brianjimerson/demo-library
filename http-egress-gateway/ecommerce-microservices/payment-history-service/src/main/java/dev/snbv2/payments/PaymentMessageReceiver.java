package dev.snbv2.payments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentMessageReceiver {

    private static final Log LOG = LogFactory.getLog(PaymentMessageReceiver.class);

    @Autowired
    PaymentRepository paymentRepository;

    public void receiveMessage(String message) throws Exception{
        
        LOG.debug(String.format("Message received = [%s]", message));
        ObjectMapper objectMapper = new ObjectMapper();
        Payment payment = null;
        
        try {
            payment = objectMapper.readValue(message, Payment.class);
        } catch (JsonProcessingException e) {
            LOG.error("Error marshalling message to Payment.", e);
            throw new Exception(e);
        }
        
        paymentRepository.save(payment);
        LOG.debug(String.format("Payment [%s] successfully saved.", payment));
    }
}
