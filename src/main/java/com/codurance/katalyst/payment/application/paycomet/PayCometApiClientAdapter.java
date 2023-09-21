package com.codurance.katalyst.payment.application.paycomet;

import com.codurance.katalyst.payment.application.paycomet.dto.CreatedUser;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentParams;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentStatus;
import com.codurance.katalyst.payment.application.paycomet.dto.PaymentBody;
import com.codurance.katalyst.payment.application.ports.PayCometApiClient;
import com.codurance.katalyst.payment.application.utils.APIClient;
import com.codurance.katalyst.payment.application.utils.DateService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class PayCometApiClientAdapter extends APIClient implements PayCometApiClient {

    public static final String API_KEY_PARAM_NAME = "PAYCOMET-API-TOKEN";
    @Value("${paycomet.urlbase}")
    private String URL_BASE;

    @Value("${paycomet.apikey}")
    private String apyKey;

    @Value("${paycomet.terminal}")
    private int terminal;

    @Autowired
    DateService dateService;

    @Override
    protected void getHeaderParameter(HttpHeaders headers) {
        headers.add(API_KEY_PARAM_NAME, apyKey);
    }

    public String generateEndPoint(String function) {
        return URL_BASE + function;
    }

    public CreatedUser createUser(String jetToken) {
        CreatedUser result = null;
        var url = generateEndPoint("/v1/cards");
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("terminal", terminal);
        requestBody.add("jetToken", jetToken);

        var request = createRequest(requestBody, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<CreatedUser> response = null;
        try {
            response = restTemplate.postForEntity(url, request, CreatedUser.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                result = response.getBody();
            }
        } catch (HttpStatusCodeException httpException) {

        }

        return result;
    }

    @Override
    public PaymentStatus payment(double amount, String currency, int idUser, String methodId, String order, String originalIp, String tokenUser) {
        String error = "";
        PaymentStatus result = null;
        try {
            //---------------------------------------------------------
            //CODIGO DE PRUEBA Y EXPLORACION DE LA API - FALTA REFACTOR
            //Siguientes pasos:
            //Revisar los parámetros que faltan y extraer logica al caso de uso
            //Revisar el formato de la referencia y como la querria el PO
            //Cubrir con test
            //Reformular los DTOs que sean necesarios
            //Eliminar de los controlles los endpoints que ya no se utilizan y eliminar
            //Del HTML también
            //21/09/2023 - We stop the development
            //---------------------------------------------------------
            var strAmount = new DecimalFormat("#.00#").format(amount);
            strAmount = strAmount.replace(".","");
            strAmount = strAmount.replace(",","");
            var instant = dateService.getInstant();
            var formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss").withZone(ZoneId.systemDefault());
            var orderStr = "PAY" +formatter.format(instant)+"1";
            var url = generateEndPoint("/v1/payments");
            var requestJSON = new PaymentBody();
            var requestBody = new PaymentParams();
            requestJSON.setPayment(requestBody);
            requestBody.setTerminal(terminal);
            requestBody.setOriginalIp(originalIp);
            requestBody.setAmount(strAmount);
            requestBody.setIdUser(idUser);
            requestBody.setOrder(orderStr);
            requestBody.setTokenUser(tokenUser);
            Gson gson = new Gson();
            var paymentBody = gson.toJson(requestJSON, PaymentBody.class);

            var request = createRequestString(paymentBody, MediaType.APPLICATION_JSON_VALUE);
            ResponseEntity<String> response = null;

            response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                result = gson.fromJson(response.getBody(), PaymentStatus.class);
            }
        } catch (HttpStatusCodeException httpException) {
            error = httpException.getMessage();
        }

        return result;
    }
}