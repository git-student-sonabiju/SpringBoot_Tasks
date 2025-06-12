package com.example.redisCaching.service;

import com.example.redisCaching.model.Product;
import com.example.redisCaching.producer.MyKafkaProducer;
import com.example.redisCaching.repository.ProductRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final MyKafkaProducer myKafkaProducer;
	private final RedisTemplate<String, Object> redisTemplate;

	public ProductService(ProductRepository productRepository, MyKafkaProducer myKafkaProducer, RedisTemplate<String, Object> redisTemplate) {
		this.productRepository = productRepository;
		this.myKafkaProducer = myKafkaProducer;
		this.redisTemplate = redisTemplate;
	}

	public Product createProduct(Product product) {
		return productRepository.save(product);
	}

	// Annotation-based implementation
//	@Cacheable(value = "products", key = "#id")
//	public Product getProductById(Long id) {
//		return productRepository.findById(id)
//				.orElseThrow(() -> new RuntimeException("Product not found."));
//	}

	// RedisTemplate-based implementation
	public Product getProductById(Long id) {
		String key = "products::" + id;
		Product cachedProduct = (Product) redisTemplate.opsForValue().get(key);
		if (cachedProduct != null) {
			return cachedProduct;
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found."));
		redisTemplate.opsForValue().set(key, product, Duration.ofSeconds(20));
		return product;
	}

	// Annotation-based implementation
//	// @Cacheable(value = "prices", key = "#id", condition = "#result > 100")
//	@Cacheable(value = "prices", key = "#id")
//	public Double getProductPrice(Long id) {
//		Product product = productRepository.findById(id)
//				.orElseThrow(() -> new RuntimeException("Product not found."));
//		return product.calculatePrice();
//	}

	// RedisTemplate-based implementation
	public Double getProductPrice(Long id) {
		String key = "prices::" + id;

		Double cachedPrice = (Double) redisTemplate.opsForValue().get(key);
		if (cachedPrice != null) {
			return cachedPrice;
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found."));
		Double price = product.calculatePrice();

		redisTemplate.opsForValue().set(key, price, Duration.ofSeconds(20));

		return price;
	}


	@CachePut(value = "products", key = "#id")
	@CacheEvict(value = "prices", key = "#id")                      //Delete the old price
	public Product updateProduct(Long id, Product product) {
		Product existingProduct = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found."));

		existingProduct.setName(product.getName());
		existingProduct.setBasePrice(product.getBasePrice());
		existingProduct.setDiscount(product.getDiscount());
		Product updatedProduct = productRepository.save(existingProduct);

		// Send Kafka update
		myKafkaProducer.sendProductUpdate(updatedProduct);

		return updatedProduct;
	}

	@PreDestroy
	@CacheEvict(value = {"products", "prices"}, allEntries = true)
	public void clearCache() {
		System.out.println("Clearing all caches");
	}
}