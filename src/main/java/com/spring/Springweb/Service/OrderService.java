/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.OrderCreateRequest;
import com.spring.Springweb.DTO.OrderResponse;
import com.spring.Springweb.Entity.Invoice;
import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request, Integer customerId);

    // Lấy đơn hàng của khách hàng hiện tại (Customer-scoped)
    List<OrderResponse> getOrdersByCustomer(Integer customerId);

    // Lấy tất cả đơn hàng Online (Admin/Staff-scoped)
    List<OrderResponse> getAllOrders();
    
    
    OrderResponse getOrderByTxnRefAndCustomer(String txnRef, Integer customerId);
    
     Invoice processOrderPaymentSuccess(String txnRef, BigDecimal amountPaid);
}
