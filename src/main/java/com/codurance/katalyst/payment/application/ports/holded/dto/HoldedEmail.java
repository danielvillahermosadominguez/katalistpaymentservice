package com.codurance.katalyst.payment.application.ports.holded.dto;

import com.codurance.katalyst.payment.application.ports.holded.exceptions.NotValidEMailFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HoldedEmail {
    public static final String AT = "@";
    public static final String SPECIAL_CHARACTERS = "[!#$%&'*+-/=?]";

    @JsonValue
    private final String value;

    @JsonIgnore
    private final String userName;

    public HoldedEmail(String email) throws NotValidEMailFormat {
        if (email == null) {
            throw createNotValidEmailException(email);
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

    private NotValidEMailFormat createNotValidEmailException(String email)  {
        return new NotValidEMailFormat("'" + email + "'" + " is not a correct format. You need to include an '@'");
    }

    private void validateFormat(String email) throws NotValidEMailFormat {
        var regexPattern = "^(.+)@(\\S+)$";
        var pattern = Pattern.compile(regexPattern);
        var matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw createNotValidEmailException(email);
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getInUnicodeFormat()  {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException exception){
            //it should happen never. We use a Runtime Exception because of this.
            throw new RuntimeException(createNotValidEmailException(value));
        }
    }

    public String getValue() {
        return value;
    }

    public static String getRecipients(List<HoldedEmail> emails) {
        List<String> emailList = emails
                .stream()
                .map(email -> email.getValue())
                .collect(Collectors.toList());
        return String.join(";", emailList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldedEmail that = (HoldedEmail) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
