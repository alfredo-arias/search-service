package com.actividadgrupal.data;

import java.util.List;

import com.actividadgrupal.model.db.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {
    List<Product> findAll();
}