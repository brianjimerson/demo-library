package dev.snbv2.order;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<CatalogOrder, Long> {
    
}
