package com.thinkitdevit.dispatch.handler;

import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.service.DispatchService;
import com.thinkitdevit.dispatch.utils.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCreatedHandlerTest {

    private OrderCreatedHandler orderCreatedHandler;

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = mock(DispatchService.class);
        orderCreatedHandler = new OrderCreatedHandler(dispatchService);
    }

    @Test
    void listen() {
        String randomUUID = UUID.randomUUID().toString();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        orderCreatedHandler.listen(payload);
        verify(dispatchService, times(1)).process(payload);
    }
}