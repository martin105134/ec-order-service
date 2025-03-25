package com.example.ecorderservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product")
@Getter@Setter
public class Product {
    @Id
    private String productId;
    private String name;
    private String description;
    private Double price;

    // Getters and Setters
}