package com.example.be.service.checkout;


import com.example.be.dto.checkout.QrInfoDTO;

public interface PaymentFlowService {
    QrInfoDTO getQrInfo(String email, Long orderId);
    void confirmPaid(String email, Long orderId);
}
