package com.codurance.katalyst.payment.application.apirest.dto;

import com.codurance.katalyst.payment.application.common.logs.AbstractLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseFactory {

    private AbstractLog log;

    @Autowired
    public ErrorResponseFactory(AbstractLog log)
    {
        this.log = log;
    }

    public ResponseEntity createBadRequest(int errorCode, String errorMessage) {
        return createResponse(errorCode, errorMessage, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity createUnprocessableRequest(int errorCode, String errorMessage) {
        return createResponse(errorCode, errorMessage, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity createResponse(int errorCode, String errorMessage, HttpStatus status) {
        return new ResponseEntity<>(
                log(new Error(errorCode, errorMessage)),
                status
        );
    }

    private Error log(Error error) {
        log.error(this.getClass(), error.getFullMessage());
        return error;
    }
}
