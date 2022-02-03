package dev.snbv2.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    
    private static final Log LOG = LogFactory.getLog(OrderController.class);

    @Autowired
    OrderRepository orderRepository;

    @GetMapping("/orders")
    public @ResponseBody List<CatalogOrder> getAllOrders() {

        Iterable<CatalogOrder> ordersIterable = orderRepository.findAll();
        List<CatalogOrder> orders = new ArrayList<CatalogOrder>();

        for (CatalogOrder o : ordersIterable) {
            orders.add(o);
        }

        LOG.debug(String.format("All orders = [%s]", orders));
        return orders;
        
    }

    @PostMapping("/order")
    public @ResponseBody CatalogOrder saveOrder(@RequestBody CatalogOrder order) {

        order = orderRepository.save(order);
        LOG.debug(String.format("Saved order [%s]", order));
        return order;
        
    }

    @GetMapping("/order/{id}")
    public @ResponseBody CatalogOrder getOrder(@PathVariable("id") Long id) {

        Optional<CatalogOrder> order = orderRepository.findById(id);

        if (order.isPresent()) {
            LOG.debug(String.format("Order retrieved = [%s]", order.get()));
        } else {
            LOG.info(String.format("No order found for id [%d]", id));
        }

        return order.get();
    }
}
