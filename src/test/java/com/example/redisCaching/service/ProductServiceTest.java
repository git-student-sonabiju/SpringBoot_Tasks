package com.example.redisCaching.service;

import com.example.redisCaching.model.Product;
import com.example.redisCaching.producer.MyKafkaProducer;
import com.example.redisCaching.repository.elastic.ProductSearchRepository;
import com.example.redisCaching.repository.jpa.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private ProductSearchRepository productSearchRepository;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ValueOperations<String, Object> valueOperations;
	@Mock
	private MyKafkaProducer myKafkaProducer;

	@InjectMocks
	private ProductService productService;

	private Product product;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		product = Product.builder()
				.id(1L)
				.name("Test Phone")
				.basePrice(1000.0)
				.discount(10.0)
				.build();
	}

	@Test
	void getAllProducts() {
		when(productRepository.findAll()).thenReturn(List.of(product));

		List<Product> result = productService.getAllProducts();

		assertThat(result).containsExactly(product);
		verify(productRepository).findAll();
	}

	@Test
	void createProduct() {
		when(productRepository.save(any())).thenReturn(product);

		Product result = productService.createProduct(product);

		assertThat(result).isEqualTo(product);
		verify(productRepository).save(product);
		verify(productSearchRepository).save(any());
	}

	@Test
	void getProductById_ShouldReturnFromCache() {
		String key = "products::1";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn(product);

		Product result = productService.getProductById(1L);

		assertThat(result).isEqualTo(product);
		verify(redisTemplate.opsForValue()).get(key);
		verify(productRepository, never()).findById(any());
	}

	@Test
	void getProductById_ShouldLoadFromDb_AndCache() {
		String key = "products::1";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn(null);
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		Product result = productService.getProductById(1L);

		assertThat(result).isEqualTo(product);
		verify(valueOperations).set(eq(key), eq(product), any());
	}

	@Test
	void getProductById_ShouldThrowWhenNotFound() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(any())).thenReturn(null);
		when(productRepository.findById(any())).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
	}

	@Test
	void getProductPrice_ShouldReturnFromCache() {
		String key = "prices::1";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn(900.0);

		Double result = productService.getProductPrice(1L);

		assertThat(result).isEqualTo(900.0);
	}

	@Test
	void getProductPrice_ShouldLoadFromDb_AndCache() {
		String key = "prices::1";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn(null);
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		Double result = productService.getProductPrice(1L);

		assertThat(result).isEqualTo(product.calculatePrice());
		verify(valueOperations).set(eq(key), eq(product.calculatePrice()), any());
	}

	@Test
	void updateProduct_ShouldUpdate_AndEvictPriceCache() {
		Product updated = Product.builder().name("Updated").basePrice(2000.0).discount(20.0).build();
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.save(any())).thenReturn(updated);

		Product result = productService.updateProduct(1L, updated);

		assertThat(result.getName()).isEqualTo("Updated");
		verify(myKafkaProducer).sendProductUpdate(any());
	}

	@Test
	void clearCache_ShouldRunWithoutExceptions() {
		assertDoesNotThrow(() -> productService.clearCache());
	}

	@Test
	void search_ShouldQueryElasticsearch() {
		Page<Product> mockPage = new PageImpl<>(List.of(product));
		when(productSearchRepository.findByNamePhraseMatch(anyString(), any())).thenReturn(mockPage);

		Page<Product> result = productService.search("Phone", 0, 5, "name", "asc");

		assertThat(result.getContent()).contains(product);
	}
}
