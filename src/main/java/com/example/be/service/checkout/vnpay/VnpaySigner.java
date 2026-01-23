package com.example.be.service.checkout.vnpay;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class VnpaySigner {

    public String hmacSha512Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String buildHashDataRaw(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();

        for (var e : sorted.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v == null || v.isBlank()) continue;

            if (hashData.length() > 0) hashData.append('&');
            hashData.append(k).append('=').append(v);
        }
        return hashData.toString();
    }


    private static String vnpEncode(String s) {
        if (s == null) return "";
        String encoded = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }


    public boolean verify(Map<String, String> params, String secret) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) return false;

        Map<String, String> copy = new TreeMap<>(params);
        copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");

        StringBuilder data = new StringBuilder();
        for (var e : copy.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v == null || v.isBlank()) continue;

            if (data.length() > 0) data.append('&');

            String encK = URLEncoder.encode(k, StandardCharsets.UTF_8);
            String encV = URLEncoder.encode(v, StandardCharsets.UTF_8);

            data.append(encK).append('=').append(encV);
        }

        String calc = hmacSha512Hex(secret.trim(), data.toString());
        return secureHash.equalsIgnoreCase(calc);
    }
}
