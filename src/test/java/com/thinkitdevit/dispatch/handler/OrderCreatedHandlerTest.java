package com.thinkitdevit.dispatch.handler;

import com.thinkitdevit.dispatch.exception.NotRetryableException;
import com.thinkitdevit.dispatch.exception.RetryableException;
import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.service.DispatchService;
import com.thinkitdevit.dispatch.utils.TestEventData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void listen_ThrowsNotRetryableException() {
        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        doThrow(new RuntimeException("Service failure")).when(dispatchService).process(key, payload);

        Exception exception = assertThrows(NotRetryableException.class,() -> orderCreatedHandler.listen(0, key, payload));

        assertThat(exception.getMessage(), equalTo("java.lang.RuntimeException: Service failure"));
        verify(dispatchService, times(1)).process(key, payload);
    }

    @Test
    void listen_ThrowsRetryableException() {
        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        doThrow(new RetryableException("Service failure")).when(dispatchService).process(key, payload);

        Exception exception = assertThrows(RetryableException.class,() -> orderCreatedHandler.listen(0, key, payload));

        assertThat(exception.getMessage(), equalTo("Service failure"));
        verify(dispatchService, times(1)).process(key, payload);
    }

}
