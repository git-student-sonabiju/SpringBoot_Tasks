package com.example.redisCaching.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MyKafkaConsumer {
	@KafkaListener(topics = "${kafka.topic}", groupId = "demo-group")
	public void consume(String message) {
		System.out.println("Consumed: " + message);
		if (message.contains("fail")) {
			throw new RuntimeException("Simulated processing failure");
		}
	}

	@KafkaListener(topics = "${kafka.dlt-topic}")
	public void consumeFromDlt(String message) {
		System.out.println("DLT received: " + message);
	}
}
