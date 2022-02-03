package dev.snbv2.catalog;

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
public class CatalogRepositoryTests {

    @Autowired
    CatalogRepository catalogRepository;
    
    @Test
    void testFindAll() throws Exception {

        List<CatalogItem> catalogItems = new ArrayList<CatalogItem>();
        for (CatalogItem catalogItem : catalogRepository.findAll()) {
            catalogItems.add(catalogItem);
        }
        
        Assertions.assertTrue(catalogItems.size() > 0);
        CatalogItem catalogItem = catalogItems.get(0);
        Assertions.assertNotNull(catalogItem.getId());
        Assertions.assertNotNull(catalogItem.getName());
        Assertions.assertNotNull(catalogItem.getDescription());
        Assertions.assertNotNull(catalogItem.getAmount());
        Assertions.assertNotNull(catalogItem.getInStock());
    }
}
