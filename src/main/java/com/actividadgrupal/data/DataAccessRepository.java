package com.actividadgrupal.data;

import com.actividadgrupal.model.db.Product;
import com.actividadgrupal.model.response.AggregationDetails;
import com.actividadgrupal.model.response.ProductsQueryResponse;
import com.actividadgrupal.utils.Consts;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.ParsedRange;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DataAccessRepository {

    // Esta clase (y bean) es la unica que usan directamente los servicios para
    // acceder a los datos.
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticClient;

    private final String[] description_fields = {"description", "description._2gram", "description._3gram"};
    private final String[] name_fields = {"name", "name._2gram", "name._3gram"};

    @SneakyThrows
    public ProductsQueryResponse findProducts(
            String name,
            String description,
            List<String> sizeValues,
            List<String> categoryValues,
            List<Double> priceValues ,
            String image,
            String page) {

        BoolQueryBuilder querySpec = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(name)) {
            querySpec.must(QueryBuilders.multiMatchQuery(name, name_fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX));
        }
        if (!StringUtils.isEmpty(description)) {
            querySpec.must(QueryBuilders.multiMatchQuery(name, description_fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX));
        }
        if (sizeValues != null && !sizeValues.isEmpty()) {
            sizeValues.forEach(
                    size -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_SIZE, size))
            );
        }
        if (categoryValues != null && !categoryValues.isEmpty()) {
            categoryValues.forEach(
                    category -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_CATEGORY, category))
            );
        }
        if (priceValues != null && !priceValues.isEmpty()) {
            priceValues.forEach(
                    price -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_PRICE, price))
            );
        }
        if (priceValues != null && !priceValues.isEmpty())
            priceValues.forEach(
                    price -> {
                        String priceString = String.valueOf(price);
                        String[] priceRange = price != null && priceString.contains("-") ? priceString.split("-") : new String[]{};

                        if (priceRange.length == 2) {
                            if ("".equals(priceRange[0])) {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_PRICE).to(priceRange[1]).includeUpper(false));
                            } else {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_PRICE).from(priceRange[0]).to(priceRange[1]).includeUpper(false));
                            }
                        } if (priceRange.length == 1) {
                            querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_PRICE).from(priceRange[0]));
                        }
                    }
            );

        if (!StringUtils.isEmpty(image)) {
            querySpec.must(QueryBuilders.matchQuery(Consts.FIELD_IMAGE, image));
        }
        //Si no se ha seleccionado ningun filtro, se añade un filtro por defecto para que la query no sea vacia
        if(!querySpec.hasClauses()) {
            querySpec.must(QueryBuilders.matchAllQuery());
        }

        //Construimos la query
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(querySpec);

        //Se incluyen las Agregaciones
        //Se incluyen las agregaciones de termino para los campos genero, designacion y estado civil
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders
                .terms(Consts.AGG_KEY_TERM_CATEGORY)
                .field(Consts.FIELD_CATEGORY).size(10000));

        //Se incluyen las agregaciones de rango para los campos edad y salario
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders
                .range(Consts.AGG_KEY_RANGE_PRICE)
                .field(Consts.FIELD_PRICE)
                .addUnboundedTo(Consts.AGG_KEY_RANGE_PRICE_0,29)
                .addRange(Consts.AGG_KEY_RANGE_PRICE_1, 29, 33)
                .addUnboundedFrom(Consts.AGG_KEY_RANGE_PRICE_2,33));

        //Se establece un maximo de 5 resultados, va acorde con el tamaño de la pagina
        nativeSearchQueryBuilder.withMaxResults(5);

        //Podemos paginar los resultados en base a la pagina que nos llega como parametro
        //El tamaño de la pagina es de 5 elementos (pero el propio llamante puede cambiarlo si se habilita en la API)
        int pageInt = Integer.parseInt(page);
        if (pageInt >= 0) {
            nativeSearchQueryBuilder.withPageable(PageRequest.of(pageInt,5));
        }

        //Se construye la query
        Query query = nativeSearchQueryBuilder.build();
        // Se realiza la busqueda
        SearchHits<Product> result = elasticClient.search(query, Product.class);
        return new ProductsQueryResponse(getResponseProducts(result), getResponseAggregations(result));
    }

    /**
     * Metodo que convierte los resultados de la busqueda en una lista de empleados.
     * @param result Resultados de la busqueda.
     * @return Lista de empleados.
     */
    private List<Product> getResponseProducts(SearchHits<Product> result) {
        return result.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    /**
     * Metodo que convierte las agregaciones de la busqueda en una lista de detalles de agregaciones.
     * Se ha de tener en cuenta que el tipo de agregacion puede ser de tipo rango o de tipo termino.
     * @param result Resultados de la busqueda.
     * @return Lista de detalles de agregaciones.
     */
    private Map<String, List<AggregationDetails>> getResponseAggregations(SearchHits<Product> result) {

        //Mapa de detalles de agregaciones
        Map<String, List<AggregationDetails>> responseAggregations = new HashMap<>();

        //Recorremos las agregaciones
        if (result.hasAggregations()) {
            Map<String, Aggregation> aggs = result.getAggregations().asMap();

            //Recorremos las agregaciones
            aggs.forEach((key, value) -> {

                //Si no existe la clave en el mapa, la creamos
                if(!responseAggregations.containsKey(key)) {
                    responseAggregations.put(key, new LinkedList<>());
                }

                //Si la agregacion es de tipo termino, recorremos los buckets
                if (value instanceof ParsedStringTerms parsedStringTerms) {
                    parsedStringTerms.getBuckets().forEach(bucket -> {
                        responseAggregations.get(key).add(new AggregationDetails(bucket.getKey().toString(), (int) bucket.getDocCount()));
                    });
                }

                //Si la agregacion es de tipo rango, recorremos tambien los buckets
                if (value instanceof ParsedRange parsedRange) {
                    parsedRange.getBuckets().forEach(bucket -> {
                        responseAggregations.get(key).add(new AggregationDetails(bucket.getKeyAsString(), (int) bucket.getDocCount()));
                    });
                }
            });
        }
        return responseAggregations;
    }

}
