package michal.malek.accounting.services;

import lombok.RequiredArgsConstructor;
import michal.malek.accounting.exceptions.PayUException;
import michal.malek.accounting.models.entities.PaymentTransaction;
import michal.malek.accounting.models.entities.UserAccount;
import michal.malek.accounting.models.payU.PayUAuth;
import michal.malek.accounting.models.payU.PayUBuyer;
import michal.malek.accounting.models.payU.PayUOrder;
import michal.malek.accounting.models.payU.PayUProduct;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PayUService {

    private final RestTemplate restTemplate;
    @Value("${payu.client-id}")
    private String client_id;
    @Value("${payu.client-secret}")
    private String client_secret;
    @Value("${payu.url.notf}")
    private String payu_url_notf;
    @Value("${payu.url.auth}")
    private String payu_url_auth;
    @Value("${payu.url.order}")
    private String payu_url_order;
    private String token;


    private void login() throws PayUException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", client_id);
        map.add("client_secret", client_secret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<PayUAuth> response =
                restTemplate.exchange(payu_url_auth,
                        HttpMethod.POST,
                        entity,
                        PayUAuth.class);
        if (response.getStatusCode().isError())
            throw new PayUException();

        token = "Bearer " + response.getBody().getAccess_token();
    }


    public String createOrder(long amount, UserAccount userAccount) throws HttpClientErrorException {
        try {
            return (String) sendOrder(amount, userAccount).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                try {
                    login();
                } catch (PayUException ex) {
                    throw new RuntimeException(ex);
                }
                return (String) sendOrder(amount, userAccount).getBody();
            }
        }
        return null;
    }

    public String checkOrderStatus(String orderId)  {
        try {
            return (String) getOrderStatus(orderId).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                try {
                    login();
                } catch (PayUException ex) {
                    throw new RuntimeException(ex);
                }
                return (String) getOrderStatus(orderId).getBody();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ResponseEntity<?> getOrderStatus(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>( headers);
        String url = payu_url_order + "/" + orderId;

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    private ResponseEntity<?> sendOrder(long amount, UserAccount userAccount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", token);

        HttpEntity<PayUOrder> requestEntity = new HttpEntity<>(prepareOrder(amount, userAccount), headers);
        return restTemplate.exchange(payu_url_order, HttpMethod.POST, requestEntity, String.class);
    }

    private PayUOrder prepareOrder(long amount, UserAccount userAccount) {
        AtomicLong totalPrice = new AtomicLong();
        totalPrice.set(amount);

        PayUBuyer buyer = new PayUBuyer(userAccount.getEmail(), userAccount.getPhone(), userAccount.getFirstName(), userAccount.getLastName());

        String productName = amount + "PLN recharge";
        PayUProduct product = new PayUProduct(productName, (int) amount, 1);

        return new PayUOrder(payu_url_notf,
                "127.0.0.1",
                client_id,
                "recharged acc with pln",
                "PLN",
                totalPrice.get(),
                UUID.randomUUID().toString(),
                buyer,
                List.of(product));
    }


    public int isPaymentGood(String orderDetails, PaymentTransaction paymentTransaction){
        String orderId = this.getOrderIdFromStatus(orderDetails);
        long amountFromOrderDetails = this.getAmountFromOrderDetails(orderDetails);

        boolean isIdOk = orderId.equals(paymentTransaction.getPayuId());
        boolean isAmountOk = (amountFromOrderDetails == paymentTransaction.getAmount());
        boolean isPaid = this.isPaid(orderDetails);
        boolean isDone = paymentTransaction.isDone();

        if(isIdOk && isAmountOk && !isDone && !isPaid){
            return 0;
        }
        if(isPaid && isIdOk && isAmountOk){
            return 1;
        }else
            return -1;
    }

    public String getOrderId(String orderDetails){
        JSONObject json = new JSONObject(orderDetails);
        return json.getString("orderId");
    }

    private String getOrderIdFromStatus(String orderDetails){
        JSONObject json = new JSONObject(orderDetails);
        JSONArray ordersArray = json.getJSONArray("orders");
        if (ordersArray != null && !ordersArray.isEmpty()) {
            JSONObject firstOrder = ordersArray.getJSONObject(0);
            return firstOrder.getString("orderId");
        }
        return null;
    }
    private long getAmountFromOrderDetails(String orderDetails) {
        JSONObject json = new JSONObject(orderDetails);
        JSONArray ordersArray = json.getJSONArray("orders");
        if (ordersArray != null && !ordersArray.isEmpty()) {
            JSONObject firstOrder = ordersArray.getJSONObject(0);
            return firstOrder.getLong("totalAmount");
        }
        return 0;
    }
    private boolean isPaid(String orderDetails) {
        try {
            JSONObject json = new JSONObject(orderDetails);

            if (!json.has("orders") || json.getJSONArray("orders").isEmpty()) {
                return false;
            }
            JSONObject firstOrder = json.getJSONArray("orders").getJSONObject(0);

            if (!firstOrder.has("status")) {
                return false;
            }
            String status = firstOrder.getString("status");

            return "COMPLETED".equals(status);
        } catch (JSONException e) {
            System.err.println("Error processing JSON: " + e.getMessage());
            return false;
        }
    }

}