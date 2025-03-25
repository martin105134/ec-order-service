package com.example.ecorderservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "orders")
@Getter
@Setter
public class Order {
    @Id
    private String orderId;
    private String orderStatus;
    private Integer orderTotal;
    private Map<String, Integer> products; // list of dish_id, quantity
    private String orderCustomer;
    private String paymentId;
}
