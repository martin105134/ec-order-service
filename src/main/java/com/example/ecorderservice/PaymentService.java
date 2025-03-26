package com.example.ecorderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    @Qualifier("payment-service")
    WebClient webClient;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    KafkaProducer kafkaProducer;

    public String createPayment(String token, PaymentRequest paymentRequest) {
        log.info("Create Payment..............");
        AtomicInteger retryCounter = new AtomicInteger(0);
        Mono<String> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION,token)
                .body(BodyInserters.fromValue(paymentRequest))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(10, Duration.ofSeconds(10))
                .doBeforeRetry(retrySignal -> {retryCounter.incrementAndGet(); log.info("Retrying..."+retryCounter);})
                .filter(throwable -> throwable instanceof RuntimeException));;

        String responseKey = paymentRequest.getOrderId() + (new Random().nextInt(1000));
        log.info("Response Key in createPayment: " + responseKey);
        redisTemplate.opsForValue().set(responseKey,"INTIATED:"+paymentRequest.getOrderId());
        response.subscribe(s ->
        {
            log.info("Order is successfully");
            String orderId = updateOrderStatus(responseKey,"PAID",s);
            redisTemplate.opsForValue().set(responseKey,"PROCESSED" + s +":" + orderId);

            try {
                kafkaProducer.sendMessage(orderId,
                        "PROCESSED",
                        "Order Processed Successfully for Order ID: " + orderId,
                        "PAYMENT SUCCESSFUL",
                        s);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        },
e -> {

            log.info("Order is Failed");
            String orderId = updateOrderStatus(responseKey,"FAILED","");
            redisTemplate.opsForValue().set(responseKey,"FAILED" + e.getMessage());
        }
        );
        return responseKey;
    }

    public String getRedisValue(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public String updateOrderStatus(String responseKey, String status, String paymentId) {
        log.info("updateOrderStatus");
        log.info("responseKey ::" + responseKey);
        String orderID = getRedisValue(responseKey).replace("INTIATED:","");
        log.info("orderID in updateOrderStatus: " + orderID);
        Order order = orderRepo.findById(orderID).get();
        log.info("order in updateOrderStatus: " + order.toString());
        order.setOrderStatus(status);
        order.setPaymentId(paymentId);
        orderRepo.save(order);
        return orderID;
    }
}
