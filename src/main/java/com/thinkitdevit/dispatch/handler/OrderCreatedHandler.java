package com.thinkitdevit.dispatch.handler;

import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreatedHandler {

    private final DispatchService dispatchService;

    @KafkaListener(id="orderConsumerClient",
            topics = "order-created",
            groupId = "dispatch",
            containerFactory = "kafkaListenerContainerFactory" )
    public void listen(OrderCreated payload){
        log.info("Received message: {}", payload);
        try{
            dispatchService.process(payload);
        }catch (Exception e){
            log.error("Error processing message", e);
        }
    }

}
