package com.example.redisCaching.repository.elastic;

import com.example.redisCaching.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<Product, Long> {
	@Query("""
			{
			  "bool": {
			    "should": [
			      { "match_phrase": { "name": "?0" } }
			    ]
			  }
			}
			""")
	Page<Product> findByNamePhraseMatch(String keyword, Pageable pageable);
}
