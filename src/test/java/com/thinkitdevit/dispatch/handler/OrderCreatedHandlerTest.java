package com.thinkitdevit.dispatch.handler;

import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.service.DispatchService;
import com.thinkitdevit.dispatch.utils.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
    void listen_Success() {
        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        orderCreatedHandler.listen(0, key, payload);
        verify(dispatchService, times(1)).process(key, payload);
    }


    @Test
    void listen_Error() {
        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        doThrow(new RuntimeException("Error")).when(dispatchService).process(key, payload);
        orderCreatedHandler.listen(0, key, payload);
        verify(dispatchService, times(1)).process(key, payload);
    }

}