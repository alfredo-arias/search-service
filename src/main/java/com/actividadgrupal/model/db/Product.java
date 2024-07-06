package com.actividadgrupal.model.db;

import com.actividadgrupal.utils.Consts;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Document(indexName = "products", createIndex = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Product {

    @Id
    private String id;

    @Field(type = FieldType.Search_As_You_Type, name = Consts.FIELD_NAME)
    private String name;

    @Field(type = FieldType.Search_As_You_Type, name = Consts.FIELD_DESCRIPTION)
    private String description;

    @Field(type = FieldType.Keyword, name = Consts.FIELD_CATEGORY)
    private String category;

    @Field(type = FieldType.Keyword, name = Consts.FIELD_SIZE)
    private String size;

    @Field(type = FieldType.Double, name = Consts.FIELD_PRICE)
    private String price;

    @Field(type = FieldType.Integer, name = Consts.FIELD_QUANTITY)
    private String quantity;

    @Field(type = FieldType.Text, name = Consts.FIELD_IMAGE)
    private String image;


}
