package com.rockburger.arquetipo2024.domain.util;

public final class DomainConstants {
    private DomainConstants() {
        throw new IllegalStateException("Utility class");
    }

    public enum Field {
        NAME,
        DESCRIPTION,
        PRICE,
        QUANTITY,

    }

    public static final String FIELD_NAME_NULL_MESSAGE = "Field 'name' cannot be null";
    public static final String FIELD_PRICE_NULL_MESSAGE = "Field 'price' cannot be null";
    public static final String FIELD_QUANTITY_NULL_MESSAGE = "Field 'quantity' cannot be null";
    public static final String FIELD_SUPPLIER_NULL_MESSAGE = "Field 'supplier' cannot be null";

    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";
    public static final String TOKEN_EXPIRED_MESSAGE = "Token has expired";
    public static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized access";
    public static final String INSUFFICIENT_PERMISSIONS_MESSAGE = "Insufficient permissions";


}
