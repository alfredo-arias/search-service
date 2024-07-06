package com.actividadgrupal.model.response;

import com.actividadgrupal.model.db.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductsQueryResponse {

    private List<Product> products;
    private Map<String, List<AggregationDetails>> aggs;

}
