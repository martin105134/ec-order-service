package com.example.ecorderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/order")
@Slf4j
public class MainRestController {

    @Autowired
    AuthService authService;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ProductService productService;

    @Autowired
    KafkaProducer kafkaProducer;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Order order, @RequestHeader("Authorization") String token, HttpServletResponse response, HttpServletRequest request) throws JsonProcessingException {
        log.info("Creating order");
        List<Cookie> cookieList = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            cookieList = new ArrayList<>();
        }else{
            cookieList = List.of(cookies);
        }

        if(cookieList.stream().filter(cookie -> cookie.getName().equals("order-first-stage")).findAny().isEmpty()) {
            if (authService.authenticate(token)) {
                log.info("Token is valid");
                order.setOrderId(String.valueOf(new Random().nextInt(1000)));
                order.setOrderTotal(productService.calculateOrder(order));
                orderRepo.save(order);
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(order.getOrderId());
                paymentRequest.setPaymentAmount(order.getOrderTotal());
                String responseKey = paymentService.createPayment(token, paymentRequest);
                log.info("Response Key: " + responseKey);
                Cookie cookieStage1 = new Cookie("order-first-stage", responseKey);
                cookieStage1.setMaxAge(10000);
                response.addCookie(cookieStage1);
                //kafkaProducer.sendMessage(authData.getUsername(),"REGISTER");
                kafkaProducer.sendMessage(order.getOrderId(),
                        "CREATE",
                        "Order Created Successfully with Order ID: " + order.getOrderId(),
                        order.getOrderStatus(),
                        order.getPaymentId());
                return ResponseEntity.ok("STAGE 1: Started processing your Order with Order ID: " + order.getOrderId());
            } else {
                return ResponseEntity.ok("Invalid token");
            }
        }else {
            Cookie followup_cookie =  cookieList.stream().
                    filter(cookie -> cookie.getName().equals("order-first-stage")).findAny().get();
            String followup_cookie_key = followup_cookie.getValue();
            String cacheResponse =paymentService.getRedisValue(followup_cookie_key);// (String)redisTemplate.opsForValue().get(followup_cookie_key);
            log.info("Cache Response: " + cacheResponse);
            if(cacheResponse.contains("INTIATED")){
                log.info("Request still under process...");
                return ResponseEntity.ok("Request still under process...");
            }else if(cacheResponse.contains("PROCESSED")){
                Cookie cookieStage1 = new Cookie("order-first-stage", null);
                cookieStage1.setMaxAge(0);
                response.addCookie(cookieStage1);
                String paymentId = cacheResponse.replace("PROCESSED", "");
                String [] data = paymentId.split(":");
                kafkaProducer.sendMessage(data[1],
                        "PROCESSED",
                        "Order Processed Successfully for Order ID: " + data[1],
                        "PAID",
                        data[0]);
                return ResponseEntity.ok("Order Created Successfully with Order ID: " + data[1] + " and Payment ID: " + data[0]);
            }else{
                return ResponseEntity.ok("Error Processing the Order");
            }
        }
    }
}
