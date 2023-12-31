package com.codurance.katalyst.payment.application.model.ports.paycomet.dto;

public class CreatedUser {
    private int idUser;
    private String tokenUser;
    private int errorCode;

    public CreatedUser() {

    }

    public CreatedUser(int idUser, String tokenUSer, int errorCode) {
        this.idUser = idUser;
        this.tokenUser = tokenUSer;
        this.errorCode = errorCode;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getTokenUser() {
        return tokenUser;
    }

    public void setTokenUser(String tokenUser) {
        this.tokenUser = tokenUser;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
