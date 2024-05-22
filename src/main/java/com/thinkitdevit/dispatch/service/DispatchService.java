package com.thinkitdevit.dispatch.service;

import com.thinkitdevit.dispatch.message.DispatchCompleted;
import com.thinkitdevit.dispatch.message.DispatchPreparing;
import com.thinkitdevit.dispatch.message.OrderDispatched;
import com.thinkitdevit.dispatch.message.OrderCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DispatchService {

    public static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    public static final String DISPATCH_TRACKING = "dispatch.tracking";

    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(String key, OrderCreated orderCreated){

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
    }

}
