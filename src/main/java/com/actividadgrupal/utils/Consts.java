package com.actividadgrupal.utils;

public final class Consts {

    private Consts() {
        throw new IllegalStateException("Utility class");
    }

    //Nombres de campos
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_SIZE = "size";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_QUANTITY = "quantity";
    public static final String FIELD_IMAGE = "image";

    //Nombres de agregaciones
    public static final String AGG_KEY_RANGE_PRICE = "priceValues";
    public static final String AGG_KEY_RANGE_PRICE_0 = "-29";
    public static final String AGG_KEY_RANGE_PRICE_1 = "29-33";
    public static final String AGG_KEY_RANGE_PRICE_2 = "33-";

    public static final String AGG_KEY_TERM_CATEGORY = "categoryValues";


}