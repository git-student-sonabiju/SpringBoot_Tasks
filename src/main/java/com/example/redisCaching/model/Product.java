package com.example.redisCaching.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Entity
public class Product implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private double basePrice;
	private double discount;

	public Double calculatePrice() {
		try {
			Thread.sleep(3000);      //Delay for 3 second
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		double tax = basePrice * 0.1;   //10% tax added
		return (basePrice + tax) - discount;
	}
}
