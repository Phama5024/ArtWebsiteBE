package com.example.be.service.order;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component("invoiceHelper")
public class InvoiceViewHelper {

    public BigDecimal lineTotal(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String money(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
}
