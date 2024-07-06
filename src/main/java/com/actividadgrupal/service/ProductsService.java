package com.actividadgrupal.service;

import com.actividadgrupal.model.response.ProductsQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.actividadgrupal.data.DataAccessRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductsService {

	private final DataAccessRepository repository;

	public ProductsQueryResponse getProducts(
			String name,
			String description,
			List<String> size,
			List<Double> price,
			List<String> category,
			String image,
			String page) {

		return repository.findProducts(
				name,
				description,
				size,
				category,
				price,
				image,
				page);
	}
}
