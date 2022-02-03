package dev.snbv2.catalog;

import org.springframework.data.repository.CrudRepository;

public interface CatalogRepository extends CrudRepository<CatalogItem, Long> {
    
}
