package com.example.ecorderservice;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Analytic {

    private String orderid;
    private String paymentid;
    private String type;
    private String description;
    private String status;
}
