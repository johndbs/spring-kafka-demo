package com.thinkitdevit.dispatch.utils;

import com.thinkitdevit.dispatch.message.OrderCreated;

public class TestEventData {

    public static OrderCreated buildOrderCreated(String orderId, String item) {
        return OrderCreated.builder()
                .orderId(orderId)
                .item(item)
                .build();
    }
}
