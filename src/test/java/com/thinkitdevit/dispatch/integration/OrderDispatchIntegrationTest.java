package com.thinkitdevit.dispatch.integration;

import com.thinkitdevit.dispatch.config.DispatchConfiguration;
import com.thinkitdevit.dispatch.message.DispatchPreparing;
import com.thinkitdevit.dispatch.message.OrderCreated;
import com.thinkitdevit.dispatch.message.OrderDispatched;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;

import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@Slf4j
@SpringBootTest(classes = {DispatchConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
@AutoConfigureWireMock(port = 0)
public class OrderDispatchIntegrationTest {

    private final static String ORDER_CREATED_TOPIC = "order-created";
    private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING = "dispatch.tracking";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private KafkaTestListener kafkaListener;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Configuration
    static class TestConfig {
        @Bean
        public KafkaTestListener kafkaListener(){
            return new KafkaTestListener();
        }

    }

    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCount = new AtomicInteger(0);
        AtomicInteger orderDispatchedCount = new AtomicInteger(0);

        @KafkaListener(topics = DISPATCH_TRACKING,
                groupId = "kafkaTest")
        void listenDispatchPreparing(
                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                @Payload DispatchPreparing payload){
            log.info("Received message - key: {} - payload: {}", key ,payload);
            dispatchPreparingCount.incrementAndGet();
        }

        @KafkaListener(topics = ORDER_DISPATCHED_TOPIC,
                groupId = "kafkaTest" )
        void listenOrderDispatched( @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                    @Payload OrderDispatched payload){
            log.info("Received message - key: {} - payload: {}", key ,payload);
            orderDispatchedCount.incrementAndGet();
        }

    }

    @BeforeEach
    public void setUp() {
        kafkaListener.dispatchPreparingCount.set(0);
        kafkaListener.orderDispatchedCount.set(0);

        WiremockUtils.reset();

        registry.getListenerContainers().stream()
                .forEach(container -> {
                    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
                });
    }

    @Test
    public void testOrderDispatchFlow() throws ExecutionException, InterruptedException {

        WiremockUtils.stubWiremock("/api/stock?item=item", 200, "true");

        UUID orderId = UUID.randomUUID();
        OrderCreated orderCreated = OrderCreated.builder()
                .orderId(orderId)
                .item("item")
                .build();
        String key = orderId.toString();

        sendMessage(ORDER_CREATED_TOPIC, key, orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.dispatchPreparingCount::get, equalTo(1));
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.orderDispatchedCount::get, equalTo(1));

    }

    @Test
    public void testOrderDispatchFlow_NotRetryableException() throws ExecutionException, InterruptedException {

        WiremockUtils.stubWiremock("/api/stock?item=item", 400, "Bad request");

        UUID orderId = UUID.randomUUID();
        OrderCreated orderCreated = OrderCreated.builder()
                .orderId(orderId)
                .item("item")
                .build();
        String key = orderId.toString();

        sendMessage(ORDER_CREATED_TOPIC, key, orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.dispatchPreparingCount::get, equalTo(0));
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.orderDispatchedCount::get, equalTo(0));

    }

    @Test
    public void testOrderDispatchFlow_RetryableException() throws ExecutionException, InterruptedException {
        WiremockUtils.stubWiremock("/api/stock?item=item", 503, "Service unavailable", "failOnce", STARTED, "succedNextTime");
        WiremockUtils.stubWiremock("/api/stock?item=item", 200, "true", "failOnce", "succedNextTime", "succedNextTime");

        UUID orderId = UUID.randomUUID();
        OrderCreated orderCreated = OrderCreated.builder()
                .orderId(orderId)
                .item("item")
                .build();
        String key = orderId.toString();

        sendMessage(ORDER_CREATED_TOPIC, key, orderCreated);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.dispatchPreparingCount::get, equalTo(1));
        await().atMost(1, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(kafkaListener.orderDispatchedCount::get, equalTo(1));

    }

    private void sendMessage(String topic, String key, Object payload) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(
                MessageBuilder.withPayload(payload)
                        .setHeader(KafkaHeaders.KEY, key)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build()
        ).get();
    }

}
