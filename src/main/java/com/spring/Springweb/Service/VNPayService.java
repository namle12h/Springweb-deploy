package com.spring.Springweb.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spring.Springweb.Entity.Invoice;
import com.spring.Springweb.util.VNPayUtil;

@Service
public class VNPayService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    // ✅ Hàm mới: nhận trực tiếp Invoice
    public String createPaymentUrl(Invoice invoice, String ipAddress) {
        if (invoice == null) {
            throw new RuntimeException("Invoice cannot be null");
        }

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        long amount = invoice.getTotal().longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", invoice.getTxnRef());
        vnp_Params.put("vnp_OrderInfo", "Thanh toán hóa đơn #" + invoice.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        String hashData = VNPayUtil.buildHashData(vnp_Params);
        String secureHash = VNPayUtil.hmacSHA512(secretKey, hashData);
        String queryUrl = VNPayUtil.buildQueryUrl(vnp_Params);
        String paymentUrl = vnp_PayUrl + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;

        System.out.println("==== FINAL URL ====");
        System.out.println(paymentUrl);

        return paymentUrl;
    }
}
