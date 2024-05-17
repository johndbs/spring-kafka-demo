package com.thinkitdevit.dispatch.service;

import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.message.OrderDispatched;
import com.thinkitdevit.dispatch.utils.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
        when(kafkaProducer.send(Mockito.anyString(), Mockito.any())).thenReturn(mock(CompletableFuture.class));

        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);
        dispatchService.process(payload);

        verify(kafkaProducer, times(1)).send(Mockito.anyString(), any());
    }

    @Test
    void process_Error() {
        UUID randomUUID = UUID.randomUUID();
        OrderCreated payload = TestEventData.buildOrderCreated(randomUUID, "item" + randomUUID);

        doThrow(new RuntimeException("Producer failed")).when(kafkaProducer).send(eq("order.dispatched"), any(OrderDispatched.class));

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> dispatchService.process(payload));

        verify(kafkaProducer, times(1)).send(Mockito.anyString(), Mockito.any());
        assertThat(exceptionThrown.getMessage()).isEqualTo("Producer failed");
    }

}