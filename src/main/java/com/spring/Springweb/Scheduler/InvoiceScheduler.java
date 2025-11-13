package com.spring.Springweb.Scheduler;

import com.spring.Springweb.Repository.InvoiceRepository;
import com.spring.Springweb.Entity.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InvoiceScheduler {

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Chạy mỗi 10 phút
    @Scheduled(fixedRate = 600000)
    public void cancelExpiredInvoices() {
        LocalDateTime now = LocalDateTime.now();
        List<Invoice> pending = invoiceRepository.findByStatusAndExpiredAtBefore("PENDING", now);
        for (Invoice inv : pending) {
            inv.setStatus("CANCELED");
            inv.setUpdatedAt(now);
            invoiceRepository.save(inv);
            System.out.println("❌ Hủy hóa đơn hết hạn #" + inv.getId());
        }
    }
}
