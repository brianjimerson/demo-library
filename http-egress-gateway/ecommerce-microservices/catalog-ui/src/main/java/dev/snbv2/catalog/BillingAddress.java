package dev.snbv2.catalog;

public class BillingAddress {

    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zipCode;

    public BillingAddress() {
    }

    public BillingAddress(String firstName, String lastName, String email, String address, String address2,
            String city, String state, String zipCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return "BillingAddress [address=" + address + ", address2=" + address2 + ", email=" + email + ", firstName="
                + firstName + ", lastName=" + lastName + ", city=" + city + ", state=" + state + ", zipCode=" + zipCode + "]";
    }
    
}
