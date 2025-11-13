/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.util.List;

import com.spring.Springweb.DTO.InvoiceCreateRequest;
import com.spring.Springweb.DTO.InvoiceResponse;
import org.springframework.data.domain.Page;

public interface InvoiceService {

    InvoiceResponse createInvoice(InvoiceCreateRequest request);

    List<InvoiceResponse> getAllInvoices();

    List<InvoiceResponse> getInvoicesByCustomer(Integer customerId);

    List<InvoiceResponse> getInvoicesByStaff(Integer staffId);

    InvoiceResponse getInvoiceByTxnRef(String txnRef);
    public Page<InvoiceResponse> getAllInvoices(int page, int size, String sortBy);


}
