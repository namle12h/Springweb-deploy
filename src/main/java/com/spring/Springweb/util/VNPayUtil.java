package com.spring.Springweb.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error while hashing", ex);
        }
    }

   public static String buildHashData(Map<String, String> fields) {
    List<String> fieldNames = new ArrayList<>(fields.keySet());
    Collections.sort(fieldNames);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fieldNames.size(); i++) {
        String name = fieldNames.get(i);
        String value = fields.get(name);
        if (value != null && !value.isEmpty()) {
            sb.append(URLEncoder.encode(name, StandardCharsets.US_ASCII))
              .append("=")
              .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            if (i < fieldNames.size() - 1) sb.append("&");
        }
    }
    return sb.toString();
}


    public static String buildQueryUrl(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
}
