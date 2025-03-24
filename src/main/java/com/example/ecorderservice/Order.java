package com.example.ecorderservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
@Getter
@Setter
public class Order {
    private String orderId;
    private String orderStatus;
    private String orderTotal;
    private String orderItems;
    private String orderCustomer;
    private String paymentId;
}
