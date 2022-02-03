package dev.snbv2.catalog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private String imageSource;

    @Column
    private String description;

    @Column
    private Double amount;

    @Column
    private Boolean inStock;

    public CatalogItem() {
    }

    public CatalogItem(Long id, String name, String imageSource, String description, Double amount, Boolean inStock) {
        this.id = id;
        this.name = name;
        this.imageSource = imageSource;
        this.description = description;
        this.amount = amount;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "CatalogItem [amount=" + amount + ", description=" + description + ", id=" + id + ", inStock=" + inStock
                + ", name=" + name + "]";
    }
    
}
