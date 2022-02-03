package dev.snbv2.catalog;

public class OrderSummary {

    private Long id;
    private String firstName;
    private String lastName;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zipCode;
    private Double amount;
    private Long catalogItemId;

    public OrderSummary() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getCatalogItemId() {
        return catalogItemId;
    }

    public void setCatalogItemId(Long catalogItemId) {
        this.catalogItemId = catalogItemId;
    }

    @Override
    public String toString() {
        return "Order [address=" + address + ", address2=" + address2 + ", amount=" + amount + ", catalogItemId="
                + catalogItemId + ", city=" + city + ", firstName=" + firstName + ", id=" + id + ", lastName="
                + lastName + ", state=" + state + ", zipCode=" + zipCode + "]";
    }

}
