package dev.snbv2.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogController {
    
    private static final Log LOG = LogFactory.getLog(CatalogController.class);

    @Autowired
    CatalogRepository catalogRepository;

    @GetMapping("/catalog")
    public @ResponseBody List<CatalogItem> getAllCatalogItems() {

        Iterable<CatalogItem> catalogItemsIterable = catalogRepository.findAll();
        List<CatalogItem> catalogItems = new ArrayList<CatalogItem>();

        for (CatalogItem ci : catalogItemsIterable) {
            catalogItems.add(ci);
        }

        LOG.debug(String.format("All catalog items = [%s]", catalogItems));
        return catalogItems;
        
    }

    @GetMapping("/catalog/{id}")
    public @ResponseBody CatalogItem getCatalogItem(@PathVariable("id") Long id) {

        Optional<CatalogItem> catalogItem = catalogRepository.findById(id);

        if (catalogItem.isPresent()) {
            LOG.debug(String.format("Catalog item retrieved = [%s]", catalogItem.get()));
        } else {
            LOG.info(String.format("No catalog item found for id [%d]", id));
        }

        return catalogItem.get();
    }
}
