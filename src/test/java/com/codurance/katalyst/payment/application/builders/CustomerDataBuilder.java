package com.codurance.katalyst.payment.application.builders;

import com.codurance.katalyst.payment.application.model.customer.CustomerData;

import java.util.Map;

public class CustomerDataBuilder {

    private static final String RANDOM_IP = "RANDOM_IP";
    private static final String RANDOM_TPV_TOKEN = "RANDOM_TPV_TOKEN";

    private CustomerData item;

    public CustomerDataBuilder createByDefault() {
        item = createCustomerDataDefault();
        return this;
    }

    public CustomerDataBuilder createFromMap(Map<String, String> data) {
        item = createCustomerDataFromMap(data);
        return this;
    }

    public CustomerData getItem() {
        return item;
    }

    private CustomerData createCustomerDataDefault() {
        var customerData = new CustomerData();
        customerData.setPaytpvToken(RANDOM_TPV_TOKEN);
        customerData.setIp(RANDOM_IP);
        return customerData;
    }

    private CustomerData createCustomerDataFromMap(Map<String, String> userData) {
        var customData = new CustomerData();
        customData.setCourseId("");
        customData.setEmail(userData.get("EMAIL"));
        customData.setName(userData.get("FIRST NAME"));
        customData.setSurname(userData.get("SURNAME"));
        customData.setCompany(userData.get("COMPANY NAME"));
        customData.setDnicif(userData.get("NIF/CIF"));
        customData.setIsCompany(userData.get("IS COMPANY").equals("YES"));
        customData.setPhoneNumber(userData.get("PHONE NUMBER"));
        customData.setAddress(userData.get("ADDRESS"));
        customData.setPostalCode(userData.get("POSTAL CODE"));
        customData.setCity(userData.get("CITY"));
        customData.setRegion(userData.get("REGION"));
        customData.setCountry(userData.get("COUNTRY"));
        customData.setUsername("");
        customData.setIp("");
        customData.setPaytpvToken("");
        return customData;
    }

    public CustomerDataBuilder ip(String value) {
        item.setIp(value);
        return this;
    }

    public CustomerDataBuilder payTpvToken(String value) {
        item.setPaytpvToken(value);
        return this;
    }

    public CustomerDataBuilder courseId(String value) {
        item.setCourseId(value);
        return this;
    }

    public CustomerDataBuilder userName(String value) {
        item.setUsername(value);
        return this;
    }
}
