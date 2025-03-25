package com.example.ecorderservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public Integer calculateOrder(Order order)
    {
        log.info("Calculating total amount for order: {}", order);
        double total = 0;
        for(Map.Entry<String, Integer> entry : order.getProducts().entrySet())
        {
            log.info("Processing dish: {}", entry.getKey());
            Product product = productRepository.findByProductId(entry.getKey());
            log.info("Found product: {}", product);
            total += product.getPrice() * entry.getValue(); // multiplying price with quantity
            log.info("Total amount so far: {}", total);
        }

        log.info("Total amount calculated: {}", total);
        return (int) total;
    }
}
