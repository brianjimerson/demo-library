package dev.snbv2.payments;

public class Payment {
    
    private String cardNumber;
    private String cvc;
    private Integer expirationMonth;
    private Integer expirationYear;
    private Double amount;
    private String currency;
    private String description;

    public Payment() {

    }

    public Payment(String cardNumber, String cvc, Integer expirationMonth, Integer expirationYear, Double amount,
            String currency, String description) {
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public Integer getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(Integer expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public Integer getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(Integer expirationYear) {
        this.expirationYear = expirationYear;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CardPayment [amount=" + amount + ", cardNumber=" + cardNumber + ", currency=" + currency + ", cvc="
                + cvc + ", description=" + description + ", expirationMonth=" + expirationMonth + ", expirationYear="
                + expirationYear + "]";
    }

}
