package com.example.be.service.checkout.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaypalHttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${paypal.base-url}") private String baseUrl;
    @Value("${paypal.client-id}") private String clientId;
    @Value("${paypal.client-secret}") private String clientSecret;
    @Value("${paypal.webhook-id}") private String webhookId;

    public String getAccessToken() {
        String url = baseUrl + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");

        JsonNode res = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(form, headers), JsonNode.class).getBody();
        if (res == null || res.get("access_token") == null) throw new RuntimeException("PayPal token failed");
        return res.get("access_token").asText();
    }

    public JsonNode createOrder(String token, String invoiceId, String currency, String amount, String returnUrl, String cancelUrl) {
        String url = baseUrl + "/v2/checkout/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "intent":"CAPTURE",
          "purchase_units":[{"invoice_id":"%s","amount":{"currency_code":"%s","value":"%s"}}],
          "application_context":{"return_url":"%s","cancel_url":"%s"}
        }
        """.formatted(invoiceId, currency, amount, returnUrl, cancelUrl);

        JsonNode res = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class).getBody();
        if (res == null || res.get("id") == null) throw new RuntimeException("PayPal create order failed");
        return res;
    }

    public JsonNode captureOrder(String token, String paypalOrderId) {
        String url = baseUrl + "/v2/checkout/orders/" + paypalOrderId + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode res = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>("{}", headers), JsonNode.class).getBody();
        if (res == null) throw new RuntimeException("PayPal capture failed");
        return res;
    }

    public boolean verifyWebhookSignature(String token,
                                          String authAlgo, String certUrl,
                                          String transmissionId, String transmissionSig, String transmissionTime,
                                          String bodyJson) {
        String url = baseUrl + "/v1/notifications/verify-webhook-signature";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = """
        {
          "auth_algo":"%s",
          "cert_url":"%s",
          "transmission_id":"%s",
          "transmission_sig":"%s",
          "transmission_time":"%s",
          "webhook_id":"%s",
          "webhook_event": %s
        }
        """.formatted(authAlgo, certUrl, transmissionId, transmissionSig, transmissionTime, webhookId, bodyJson);

        JsonNode res = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, headers), JsonNode.class).getBody();
        return res != null && res.get("verification_status") != null
                && "SUCCESS".equalsIgnoreCase(res.get("verification_status").asText());
    }
}
