package com.thinkitdevit.dispatch.service;

import com.thinkitdevit.dispatch.client.StockServiceClient;
import com.thinkitdevit.dispatch.message.DispatchCompleted;
import com.thinkitdevit.dispatch.message.DispatchPreparing;
import com.thinkitdevit.dispatch.message.OrderDispatched;
import com.thinkitdevit.dispatch.message.OrderCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DispatchService {

    public static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    public static final String DISPATCH_TRACKING = "dispatch.tracking";

    private final KafkaTemplate<String, Object> kafkaProducer;

    private final StockServiceClient stockServiceClient;

    public void process(String key, OrderCreated orderCreated){

        String availableStock = stockServiceClient.checkAvailability(orderCreated.getItem());

        if(Boolean.valueOf(availableStock)){
            DispatchPreparing dispatchPreparing = DispatchPreparing.builder()
                    .orderId(orderCreated.getOrderId())
                    .build();

            kafkaProducer.send(DISPATCH_TRACKING, key, dispatchPreparing);

            OrderDispatched orderDispatched = OrderDispatched.builder()
                    .orderId(orderCreated.getOrderId())
                    .build();

            kafkaProducer.send(ORDER_DISPATCHED_TOPIC, key, orderDispatched);

            DispatchCompleted dispatchCompleted = DispatchCompleted.builder()
                    .orderId(orderCreated.getOrderId())
                    .distpatchedDate(LocalDate.now().toString())
                    .build();

            kafkaProducer.send(DISPATCH_TRACKING, key, dispatchCompleted);
        }else{
            log.error("Item {} is not available", orderCreated.getItem());
        }

    }

}
