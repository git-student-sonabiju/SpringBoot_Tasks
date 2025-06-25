package com.example.redisCaching.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Document(indexName = "products") //ElasticSearch
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Entity
public class Product implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@org.springframework.data.annotation.Id // Annotation for elasticsearch
	private Long id;

	@Field(type = FieldType.Text)
	private String name;

	private double basePrice;
	private double discount;

	public Double calculatePrice() {
		try {
			Thread.sleep(3000); // Simulated delay
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		double tax = basePrice * 0.1;
		return (basePrice + tax) - discount;
	}
}
