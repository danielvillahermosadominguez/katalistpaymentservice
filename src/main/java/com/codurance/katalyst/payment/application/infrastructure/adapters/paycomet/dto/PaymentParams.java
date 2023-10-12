package com.codurance.katalyst.payment.application.infrastructure.adapters.paycomet.dto;

public class PaymentParams {
       private int terminal;
       private String amount;
       private String currency = "EUR";
       private int idUser;
       private String methodId="1";
       private String originalIp;
       private int secure =1; //1 = seguro 0 = no seguro
       private String order;
       private String tokenUser;
       private String productDescription= "Katalyst subscription";
       private String merchantDescriptor= "Katalyst subscription";
       //private int userInteraction = 1;
       //private int notifyDirectPayment = 1;
       //private int tokenize = 1;

        public int getTerminal() {
                return terminal;
        }

        public void setTerminal(int terminal) {
                this.terminal = terminal;
        }

        public String getAmount() {
                return amount;
        }

        public void setAmount(String amount) {
                this.amount = amount;
        }

        public int getIdUser() {
                return idUser;
        }

        public void setIdUser(int idUser) {
                this.idUser = idUser;
        }

        public int getSecure() {
                return secure;
        }

        public void setSecure(int secure) {
                this.secure = secure;
        }

        public String getOrder() {
                return order;
        }

        public void setOrder(String order) {
                this.order = order;
        }

        public String getTokenUser() {
                return tokenUser;
        }

        public void setTokenUser(String tokenUser) {
                this.tokenUser = tokenUser;
        }

    public String getOriginalIp() {
        return originalIp;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }
}
