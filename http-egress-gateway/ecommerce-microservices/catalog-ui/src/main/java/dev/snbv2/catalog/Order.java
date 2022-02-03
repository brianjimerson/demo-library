package dev.snbv2.catalog;

public class Order {
    
    private CatalogItem catalogItem;
    private BillingAddress billingAddress;
    private Payment payment;

    public CatalogItem getCatalogItem() {
        return catalogItem;
    }

    public void setCatalogItem(CatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "Order [billingAddress=" + billingAddress + ", catalogItem=" + catalogItem + ", payment=" + payment
                + "]";
    }



}
