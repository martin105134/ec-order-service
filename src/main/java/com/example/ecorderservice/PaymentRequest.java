package com.example.ecorderservice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private String orderId;
    private String paymentId;
    private Integer paymentAmount;
}
