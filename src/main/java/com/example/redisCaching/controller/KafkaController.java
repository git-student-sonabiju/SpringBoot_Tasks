package com.example.redisCaching.controller;

import com.example.redisCaching.producer.MyKafkaProducer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

	private final MyKafkaProducer producer;

	public KafkaController(MyKafkaProducer producer) {
		this.producer = producer;
	}

	@PostMapping("/send")
	public String sendMessage(@RequestBody String message) {
		producer.sendMessage(message);
		return "Message sent: " + message;
	}
}
