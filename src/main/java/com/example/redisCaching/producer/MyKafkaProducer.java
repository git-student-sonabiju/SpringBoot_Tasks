package com.example.redisCaching.producer;

import com.example.redisCaching.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MyKafkaProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${kafka.topic}")
	private String topic;

	public MyKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = new ObjectMapper();
	}

	public void sendProductUpdate(Product product) {
		try {
			String json = objectMapper.writeValueAsString(product); // convert Product -> JSON
			kafkaTemplate.send(topic, json); // send JSON to Kafka topic
			System.out.println("Kafka message sent: " + json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		kafkaTemplate.send(topic, message);
		System.out.println("produced: " + message);
	}
}
