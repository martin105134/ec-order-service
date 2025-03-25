package com.example.ecorderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String orderid,
                            String type,
                            String description,
                            String status,
                            String paymentid) throws JsonProcessingException {
        log.debug("Entering KafkaProducer.sendMessage");
        Analytic analytical = new Analytic();
        analytical.setOrderid(orderid);
        analytical.setType(type);
        analytical.setDescription(description);
        analytical.setStatus(status);
        analytical.setPaymentid(paymentid);
        ObjectMapper mapper = new ObjectMapper();
        log.debug(mapper.writeValueAsString(analytical));
        kafkaTemplate.send("order-event",mapper.writeValueAsString(analytical));
        log.debug("Exiting KafkaProducer.sendMessage");
    }
}
