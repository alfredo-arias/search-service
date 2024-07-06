package com.actividadgrupal.controller;

import com.actividadgrupal.model.response.ProductsQueryResponse;
import com.actividadgrupal.service.ProductsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ProductsController {

    private final ProductsService service;

    @GetMapping("/products")
    public ResponseEntity<ProductsQueryResponse> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<String> sizeValues,
            @RequestParam(required = false) List<Double> priceValues,
            @RequestParam(required = false) List<String> categoryValues,
            @RequestParam(required = false) String image,
            @RequestParam(required = false, defaultValue = "0") String page) {

        ProductsQueryResponse response = service.getProducts(
                name,
                description,
                sizeValues,
                priceValues,
                categoryValues,
                image,
                page);
        return ResponseEntity.ok(response);
    }
}
