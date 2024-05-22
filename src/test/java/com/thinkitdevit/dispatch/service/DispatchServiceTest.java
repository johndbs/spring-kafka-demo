package com.thinkitdevit.dispatch.service;

import com.thinkitdevit.dispatch.message.DispatchCompleted;
import com.thinkitdevit.dispatch.message.DispatchPreparing;
import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.message.OrderDispatched;
import com.thinkitdevit.dispatch.utils.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatchServiceTest {


    private KafkaTemplate<String, Object> kafkaProducer;


    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        kafkaProducer = mock(KafkaTemplate.class);
        dispatchService = new DispatchService(kafkaProducer);
    }

    @Test
    void process_Success() {
        when(kafkaProducer.send(Mockito.anyString(), Mockito.any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducer.send(Mockito.anyString(), Mockito.any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducer.send(Mockito.anyString(), Mockito.any(DispatchCompleted.class))).thenReturn(mock(CompletableFuture.class));

        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        dispatchService.process(key, payload);

        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducer, times(1)).send(eq("order.dispatched"),eq(key),  any(OrderDispatched.class));
        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchCompleted.class));
    }

    @Test
    void process_DispatchThrackingThrowsException() {
        String key = UUID.randomUUID().toString();
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);

        doThrow(new RuntimeException("dispatch.tracking failed")).when(kafkaProducer).send(eq("dispatch.tracking"),eq(key), any(DispatchPreparing.class));

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> dispatchService.process(key, payload));

        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), Mockito.any());
        assertThat(exceptionThrown.getMessage()).isEqualTo("dispatch.tracking failed");
    }


    @Test
    void process_OrderCreatedThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);

        when(kafkaProducer.send(eq("dispatch.tracking"), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducer.send(Mockito.anyString(), Mockito.any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));

        doThrow(new RuntimeException("order.dispatched failed")).when(kafkaProducer).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> dispatchService.process(key, payload));

        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), Mockito.any());
        verify(kafkaProducer, times(1)).send(eq("order.dispatched"), eq(key),  Mockito.any());
        assertThat(exceptionThrown.getMessage()).isEqualTo("order.dispatched failed");
    }

    @Test
    void process_DispatchCompletedThrowsException() {
        UUID randomUUID = UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);

        when(kafkaProducer.send(eq("dispatch.tracking"), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));


        doThrow(new RuntimeException("dispatch.tracking failed")).when(kafkaProducer).send(eq("dispatch.tracking"), eq(key), any(DispatchCompleted.class));

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> dispatchService.process(key, payload));

        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), Mockito.any(DispatchPreparing.class));
        verify(kafkaProducer, times(1)).send(eq("order.dispatched"), eq(key),  Mockito.any(OrderDispatched.class));
        verify(kafkaProducer, times(1)).send(eq("dispatch.tracking"), eq(key), Mockito.any(DispatchCompleted.class));
        assertThat(exceptionThrown.getMessage()).isEqualTo("dispatch.tracking failed");
    }

}