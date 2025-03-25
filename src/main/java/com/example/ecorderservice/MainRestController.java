package com.example.ecorderservice;

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

    /*@Autowired
    RedisTemplate<Object, Object> redisTemplate;*/

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Order order, @RequestHeader("Authorization") String token, HttpServletResponse response, HttpServletRequest request) {
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
                orderRepo.save(order);
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(order.getOrderId());
                paymentRequest.setPaymentAmount(new Random().nextInt(1000));
                String responseKey = paymentService.createPayment(token, paymentRequest);
                Cookie cookieStage1 = new Cookie("order-first-stage", responseKey);
                cookieStage1.setMaxAge(10000);
                response.addCookie(cookieStage1);
                return ResponseEntity.ok("Started processing the order stage 1");
            } else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        }else {
            Cookie followup_cookie =  cookieList.stream().
                    filter(cookie -> cookie.getName().equals("order-first-stage")).findAny().get();
            String followup_cookie_key = followup_cookie.getValue();
            String cacheResponse =paymentService.getRedisValue(followup_cookie_key);// (String)redisTemplate.opsForValue().get(followup_cookie_key);
            if(cacheResponse.contains("INTIATED")){
                log.info("Request still under process...");
                return ResponseEntity.ok("Request still under process...");
            }else if(cacheResponse.contains("PROCESSED")){
                return ResponseEntity.ok("Order Created Successfully with Order ID: " + order.getOrderId() + " and Payment ID: " + cacheResponse.replace("PROCESSED", ""));
            }else{
                return ResponseEntity.ok("Error Processing the Order");
            }
        }
    }
}
