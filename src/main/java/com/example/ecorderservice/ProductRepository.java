package com.example.ecorderservice;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, Integer> {
    Product findByProductId(String id);
}
