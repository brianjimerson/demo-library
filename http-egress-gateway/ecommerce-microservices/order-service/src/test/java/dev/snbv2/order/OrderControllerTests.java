package dev.snbv2.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderControllerTests {
    
    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGetAllOrders() throws Exception {

        mvc.perform(get("/orders")).
            andDo(MockMvcResultHandlers.print()).
            andExpect(status().isOk());

    }
    
    @Test
    void testGetCatalogItems() throws Exception {

    String order = "{" + 
        "\"firstName\": \"First name\"," + 
        "\"lastName\": \"Last name\"," + 
        "\"address\": \"Address\"," + 
        "\"address2\": \"Address 2\"," + 
        "\"city\": \"City\"," + 
        "\"state\": \"State\"," + 
        "\"zipCode\": \"Zip code\"," + 
        "\"amount\": \"101.55\"," + 
        "\"catalogItemId\": \"1\"" + 
        "}";
    
    mvc.perform(post("/order").
        contentType("application/json").
        content(order)).
        andDo(MockMvcResultHandlers.print()).
        andExpect(status().isOk()).
        andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());

    }

}
