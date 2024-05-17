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
    void listen_Success() {
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        orderCreatedHandler.listen(payload);
        verify(dispatchService, times(1)).process(payload);
    }


    @Test
    void listen_Error() {
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        doThrow(new RuntimeException("Error")).when(dispatchService).process(payload);
        orderCreatedHandler.listen(payload);
        verify(dispatchService, times(1)).process(payload);
    }

}