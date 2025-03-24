package com.example.ecorderservice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean(name = "auth-service-validate")
    public WebClient webClientAuthService(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl("http://localhost:8090/api/v1/validate")
                .build();
    }

    @Bean(name = "payment-service")
    public WebClient webClientPaymentService(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl("http://localhost:8094/payment/create")
                .build();
    }
}