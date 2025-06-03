package com.example.redisCaching.controller;

import com.example.redisCaching.model.Product;
import com.example.redisCaching.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	public Product createProduct(@RequestBody Product product) {
		return productService.createProduct(product);
	}

	@PutMapping("/{id}")
	public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
		return productService.updateProduct(id, product);
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		return productService.getProductById(id);
	}

	@GetMapping("/{id}/price")
	public Double getProductPrice(@PathVariable Long id) {
		return productService.getProductPrice(id);
	}

	@DeleteMapping("/cache")
	public String clearCache() {
		productService.clearCache();
		return "clear caches";
	}
}
