package dev.snbv2.payments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentMessageService {

    @Autowired
    AmqpTemplate amqpTemplate;

    private static final Log LOG = LogFactory.getLog(PaymentMessageService.class);

    public void sendMessage(Payment payment) throws Exception {

        ObjectMapper paymentMapper = new ObjectMapper();
        String paymentJson = null;
        try {
            paymentJson = paymentMapper.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting Payment to JSON.", e);
            throw new Exception(e);
        }
        
        LOG.debug(String.format("Sending payment %s", paymentJson));
        amqpTemplate.convertAndSend("payments", paymentJson);

    }
    
}
