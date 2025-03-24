package com.example.ecorderservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    @Autowired
    @Qualifier("auth-service-validate")
    WebClient webClient;

    public boolean authenticate(String token) {
        String response = webClient.get()
                .header(HttpHeaders.AUTHORIZATION,token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
             return response.equalsIgnoreCase("valid");
    }
}
