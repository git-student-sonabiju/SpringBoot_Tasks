package com.example.redisCaching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.redisCaching.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "com.example.redisCaching.repository.elastic")
public class RedisCachingApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisCachingApplication.class, args);
	}

}
