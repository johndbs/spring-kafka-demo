package com.thinkitdevit.dispatch.handler;

import com.thinkitdevit.dispatch.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        orderCreatedHandler.listen("payload");
        verify(dispatchService, times(1)).process("payload");
    }
}