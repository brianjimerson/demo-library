package dev.snbv2.catalog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class CatalogController {

    private static final Log LOG = LogFactory.getLog(CatalogController.class);

    // REST endpoints
    private static final String CATALOG_SERVICE_ENDPOINT = "http://catalog-service:8080";
    private static final String PAYMENT_SERVICE_ENDPOINT = "http://payment-service:8080";
    private static final String ORDER_SERVICE_ENDPOINT = "http://order-service:8080";

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/itemDetails")
    public String getCatalogItem(@RequestParam("id") Long id, Model model) {
        
        CatalogItem item = restTemplate.getForObject(
            CATALOG_SERVICE_ENDPOINT + "/catalog/{id}", CatalogItem.class, id);
       
        
        LOG.debug(String.format("Catalog item retrieved = [%s]", item));

        model.addAttribute("item", item);
        return "itemDetails";
    }

    @GetMapping("/catalog")
    public String getAllCatalogItems(Model model) {

        CatalogItem[] catalogItems = restTemplate.getForEntity(
            CATALOG_SERVICE_ENDPOINT +  "/catalog", CatalogItem[].class).getBody();

        model.addAttribute("catalogItems", catalogItems);
        return "catalog";
    }

    @GetMapping("/order")
    public String orderItem(@RequestParam("id") Long id, Model model) {
        
        CatalogItem item = restTemplate.getForObject(
            CATALOG_SERVICE_ENDPOINT + "/catalog/{id}", CatalogItem.class, id);
        
        LOG.debug(String.format("Catalog item retrieved = [%s]", item));

        Order order = new Order();
        order.setCatalogItem(item);
        LOG.debug(String.format("Order = [%s]", order));

        model.addAttribute("order", order);
        return "order";
    }

    @PostMapping("/order")
    public String placeOrder(@ModelAttribute Order order, Model model) {

        LOG.debug(String.format("Order being placed = [%s]", order));
        String result = restTemplate.postForObject(
            PAYMENT_SERVICE_ENDPOINT + "/payment", order.getPayment(), String.class);
        
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.setAddress(order.getBillingAddress().getAddress());
        orderSummary.setAddress2(order.getBillingAddress().getAddress2());
        orderSummary.setAmount(order.getPayment().getAmount());
        orderSummary.setCatalogItemId(order.getCatalogItem().getId());
        orderSummary.setCity(order.getBillingAddress().getCity());
        orderSummary.setFirstName(order.getBillingAddress().getFirstName());
        orderSummary.setLastName(order.getBillingAddress().getLastName());
        orderSummary.setState(order.getBillingAddress().getState());
        orderSummary.setZipCode(order.getBillingAddress().getZipCode());

        OrderSummary savedOrder = restTemplate.postForObject(
            ORDER_SERVICE_ENDPOINT + "/order", orderSummary, OrderSummary.class);

        LOG.debug(String.format("Result of order = [%s]", savedOrder));
        model.addAttribute("result", result);
        model.addAttribute("order", order);
        return "orderResult";
    }
}
