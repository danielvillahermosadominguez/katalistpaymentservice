package com.codurance.katalyst.payment.application.model.payment.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CARDS(1),
    PAYPAL(10),
    BIZUM(11),
    IDEAL(12),
    KLARNA_PAYNOW(13),
    GIROPLAY(14),
    MYBANK(15),
    MULTIBANCOSIBS(16),
    TRUSTLY(17),
    PREZELEWY24(18),
    BANCONTACT(19),
    EPS(20),
    PAYSERA(22),
    POSTFINANCE(23),
    QIWIWALLET(24),
    YANDEXMONEY(25),
    BEELINE(27),
    PAYSAFECARD(28),
    SKRILL(29),
    WEBMONEY(30),
    INSTANTCREDIT(33),
    KLARNAPAYMENTS(34),
    SEPA_DIRECT_DEBIT(35),
    MBWAY(38),
    NUAPAY(40),
    WAYLET(41),
    INSTANT_TRANSFER(42);

    private int value;

    PaymentMethod(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return this.value;
    }

    public static PaymentMethod fromInt(int value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if(method.getValue() == value) {
                return method;
            }
        }
        return null;
    }
}
