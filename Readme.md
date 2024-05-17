# Kafka springboot demo


## Description

This is a simple springboot application that demonstrates how to use Kafka with springboot. The application has two endpoints, one to produce messages and the other to consume messages. The application uses the `@KafkaListener` annotation to consume messages from a Kafka topic. The application uses the `KafkaTemplate` to produce messages to a Kafka topic.

## Pre-requisites

### Kafka

Create a topic with the following command:

```bash
kafka-topics.bat --bootstrap-server localhost:9092 --create --topic order-created
```

Send a message to the topic with the following command:

```bash
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order-created
```
Message example:
```json
{"orderId": "f6ade72d-d6d2-4bbb-8543-08a35b830884", "item":"item-1"}
```

Read messages from the topic with the OrderCreatedHandlerTest or the following command:

```bash
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-created --group dispatch
```