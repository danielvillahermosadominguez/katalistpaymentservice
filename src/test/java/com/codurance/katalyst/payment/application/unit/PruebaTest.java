package com.codurance.katalyst.payment.application.unit;

import com.codurance.katalyst.payment.application.holded.dto.HoldedBillAddress;
import com.codurance.katalyst.payment.application.holded.dto.HoldedContact;
import com.codurance.katalyst.payment.application.holded.dto.HoldedEmail;
import com.codurance.katalyst.payment.application.holded.dto.HoldedTypeContact;
import com.codurance.katalyst.payment.application.holded.dto.NotValidEMailFormat;
import com.codurance.katalyst.payment.application.holded.requests.CreateContactRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PruebaTest {

    @Test public void
    enum_to_String() {
        var typeContact = HoldedTypeContact.CLIENT;
        var name = typeContact.getName();
        assertThat(name).isEqualTo("client");
    }

    @Test public void
    string_to_object() throws JsonProcessingException {
        //{"id":1,"customId":"46842041CRANDOM_USERNAME%2540email.com","email":"RANDOM_USERNAME@email.com","name":"RANDOM_NAME","code":"46842041C","type":"client"}
        var s = "{\"id\":1,\"customId\":\"46842041CRANDOM_USERNAME%2540email.com\",\"email\":\"RANDOM_USERNAME@email.com\",\"name\":\"RANDOM_NAME\",\"code\":\"46842041C\",\"type\":\"client\"}";
        var objectMapper = new ObjectMapper();
        var contact = objectMapper.readValue(s,HoldedContact.class);
    }
    @Test
    public void
    object_to_string() throws NotValidEMailFormat, JsonProcessingException {
        var gson = new Gson();
        var billingAddress = new HoldedBillAddress(
                "Avenida de los poblados, 21, 2B",
                "28080",
                "Boadilla",
                "Madrid",
                "Spain");
        var contact = new HoldedContact(
                "John Doe",
                "46842041C",
                HoldedTypeContact.CLIENT,
                true,
                new HoldedEmail("john.doe@email.com"),
                "636638359",
                billingAddress,
                "70500000"
        );
        var gsonString = gson.toJson(contact);
        var objectMapper = new ObjectMapper();
        var jacksonString = objectMapper.writeValueAsString(contact);
        assertThat("").isEqualTo(jacksonString);
    }

    @Test
    public void
    create_to_string() throws NotValidEMailFormat, JsonProcessingException {
        var gson = new Gson();
        var contact = new HoldedContact(
                "John Doe",
                "46842041C",
                HoldedTypeContact.CLIENT,
                true,
                new HoldedEmail("john.doe@email.com"),
                "636638359",
                null,
                "70500000"
        );
        var createContactBody = new CreateContactRequestBody(contact);
        var objectMapper = new ObjectMapper();
        var jacksonString = objectMapper.writeValueAsString(createContactBody);
        assertThat("").isEqualTo(jacksonString);
    }
}
