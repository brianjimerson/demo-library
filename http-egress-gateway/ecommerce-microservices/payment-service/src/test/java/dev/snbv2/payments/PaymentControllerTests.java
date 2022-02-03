package dev.snbv2.payments;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class PaymentControllerTests {
    
    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @Test
    void testProcessPayment() throws Exception {

        String payment = "{" + 
            "\"cardNumber\": \"5555555555554444\"," + 
            "\"cvc\": \"111\"," + 
            "\"expirationMonth\": \"12\"," + 
            "\"expirationYear\": \"25\"," + 
            "\"amount\": \"101.55\"," + 
            "\"currency\": \"usd\"," + 
            "\"description\": \"test payment\"" + 
        "}";
        
        mvc.perform(post("/payment").
            contentType("application/json").
            content(payment)).
            andDo(MockMvcResultHandlers.print()).
            andExpect(status().isOk()).
            andExpect(MockMvcResultMatchers.content().string("succeeded"));

    }

}
