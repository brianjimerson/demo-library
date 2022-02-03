package dev.snbv2.payments;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PaymentRepositoryTests {

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    void testSave() {

        Payment payment = new Payment();
        payment.setAmount(55.00);
        payment.setCardNumber("5555555555554444");
        payment.setCurrency("usd");
        payment.setCvc("999");
        payment.setDescription("Test payment");
        payment.setExpirationMonth(12);
        payment.setExpirationYear(24);

        payment = paymentRepository.save(payment);

        Optional<Payment> payment2 = paymentRepository.findById(payment.getId());
        Assertions.assertTrue(payment2.isPresent());
        Assertions.assertNotNull(payment2.get().getId());
        Assertions.assertTrue(payment2.get().getCardNumber().equalsIgnoreCase("5555555555554444"));
        
    }
    
}
