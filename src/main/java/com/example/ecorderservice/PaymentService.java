package com.example.ecorderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class PaymentService {

    @Autowired
    @Qualifier("payment-service")
    WebClient webClient;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public String createPayment(String token, PaymentRequest paymentRequest) {
        Mono<String> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION,token)
                .body(BodyInserters.fromValue(paymentRequest))
                .retrieve()
                .bodyToMono(String.class);

        String responseKey = paymentRequest.getOrderId() + (new Random().nextInt(1000));
        response.subscribe(s -> redisTemplate.opsForValue().set(responseKey,"payment_id" + s));

        return responseKey;
    }
}
