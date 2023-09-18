package com.codurance.katalyst.payment.application.holded.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HoldedEmail {
    public static final String AT = "@";
    public static final String SPECIAL_CHARACTERS = "[!#$%&'*+-/=?]";
    private final String value;
    private final String userName;

    public HoldedEmail(String email) throws NotValidEMailFormat {
        if (email == null) {
            throwNotValidEmailException(email);
        }

        validateFormat(email);
        this.userName = extractUserName(email);
        this.value = email;
    }

    private String extractUserName(String email) throws NotValidEMailFormat {
        var result = "";
        String arrayOfStr[] = email.split(AT);
        if (arrayOfStr.length == 2) {
            result = arrayOfStr[0];
        }

        result = result.replaceAll(SPECIAL_CHARACTERS, "");
        if (result.isEmpty()) {
            throw new NotValidEMailFormat("'" + email + "'" + " has not a correct username:" + result);
        }
        return result;
    }

    private void throwNotValidEmailException(String email) throws NotValidEMailFormat {
        throw new NotValidEMailFormat("'" + email + "'" + " is not a correct format. You need to include an '@'");
    }

    private void validateFormat(String email) throws NotValidEMailFormat {
        var regexPattern = "^(.+)@(\\S+)$";
        var pattern = Pattern.compile(regexPattern);
        var matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throwNotValidEmailException(email);
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getInUnicodeFormat() throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    public String getValue() {
        return value;
    }

    public static String getRecipients(List<HoldedEmail> emails) {
        List<String> emailList = emails
                .stream()
                .map( email-> email.getValue())
                .collect(Collectors.toList());
        return String.join(";", emailList);
    }
}
