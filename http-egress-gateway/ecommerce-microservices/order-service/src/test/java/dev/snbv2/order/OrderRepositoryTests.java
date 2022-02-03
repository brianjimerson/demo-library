package dev.snbv2.order;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderRepositoryTests {

    @Autowired
    OrderRepository orderRepository;
    
    @Test
    void testFindAll() throws Exception {

        List<CatalogOrder> catalogItems = new ArrayList<CatalogOrder>();
        for (CatalogOrder catalogItem : orderRepository.findAll()) {
            catalogItems.add(catalogItem);
        }
        
        Assertions.assertTrue(catalogItems.size() == 0);
    }

    @Test
    void testSave() throws Exception {

        CatalogOrder order = new CatalogOrder();
        order.setFirstName("First name");
        order.setLastName("Last name");
        order.setAddress("Address");
        order.setAddress2("Address 2");
        order.setCity("City");
        order.setState("State");
        order.setZipCode("Zip code");
        order.setAmount(123.45D);
        order.setCatalogItemId(1L);

        order = orderRepository.save(order);
        Assertions.assertTrue(order.getId() != null);
    }
}
