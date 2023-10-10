package com.codurance.katalyst.payment.application.ports.holded.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HoldedContact {
    @JsonProperty("isperson")
    private boolean isPerson;

    protected String id;

    protected String customId;

    protected String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("vatnumber")
    private String vatNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phone;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected HoldedEmail email;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected HoldedTypeContact type;

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private HoldedBillAddress billAddress;

    @JsonIgnore
    private String purchaseAccount;

    public HoldedContact() {

    }

    public HoldedContact(String name,
                         String code,
                         String vatNumber,
                         HoldedTypeContact type,
                         boolean isPerson,
                         HoldedEmail email,
                         String phone,
                         HoldedBillAddress billAddress,
                         String purchaseAccount) {

        this.name = name;
        this.code = code;
        this.vatNumber = vatNumber;
        this.type = type;
        this.isPerson = isPerson;
        this.email = email;
        this.phone = phone;
        this.billAddress = billAddress;
        this.purchaseAccount = purchaseAccount;
        this.customId = isPerson
                ? buildCustomId(code, email)
                : buildCustomId(vatNumber, email);
    }

    public static String buildCustomId(String codeOrVat, HoldedEmail email) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-256");
            var input = codeOrVat + email.getValue();
            var hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        var hexString = "";
        for (var item : hash) {
            var hex = Integer.toHexString(0xff & item);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            hexString += hex;
        }
        return hexString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomId() {
        return customId;
    }

    public String getName() {
        return name;
    }

    public HoldedEmail getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public HoldedTypeContact getType() {
        return type;
    }

    public String getPhone() {
        return phone;
    }

    public HoldedBillAddress getBillAddress() {
        return billAddress;
    }

    @JsonIgnore
    public String getPurchaseAccount() {
        return purchaseAccount;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public boolean isPerson() {
        return isPerson;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public boolean haveSameMainData(HoldedContact contact) {
        // We don't have into account the id neither purchaseAccount. It is not an equal.
        var result = contact.getCustomId().equals(customId);
        result &= contact.getEmail().getValue().equals(email.getValue());
        result &= contact.getCode() != null
                ? contact.getCode().equals(code)
                : code == null;
        result &= contact.getVatNumber() != null
                ? contact.getVatNumber().equals(vatNumber)
                : vatNumber == null;
        result &= contact.getName().equals(name);
        result &= contact.getType() == type;
        result &= contact.isPerson() == isPerson;
        result &= contact.getPhone().equals(phone);
        result &= contact.getBillAddress().equals(billAddress);
        return result;
    }

}
