package com.rockburger.arquetipo2024.configuration;



public class Constants {
    private Constants(){
        throw new IllegalStateException("utility class");
    }

    public static final String NAME_ALREADY_EXIST = "This name already exists";
    public static final String NO_DATA_FOUND_EXCEPTION_MESSAGE = "No data was found in the database";
    public static final String ELEMENT_NOT_FOUND_EXCEPTION_MESSAGE = "The element indicated does not exist";
    public static final String EMPTY_FIELD_EXCEPTION_MESSAGE = "Field %s can not be empty";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "That is an invalid argument";
    public static final String INVALID_PAGE_PARAMETER_MESSAGE = "Invalid page parameter";
    public static final String INVALID_CATEGORY_COUNT_MESSAGE = "Articles could have between 1 and 3 categories";
    public static final String DUPLICATE_CATEGORY_MESSAGE = "The Category %s is repeated";
    public static final String NEGATIVE_NOT_ALLOWED_EXCEPTION_MESSAGE = "Field %s can not receive negative values";

    public static final String BLANK_FIELD_ERROR = "The field cannot be blank.";
    public static final String INVALID_AGE_MESSAGE = "User must be at least 18 years old";
    public static final String DUPLICATE_EMAIL_MESSAGE = "Email already exists";
    public static final String DUPLICATE_ID_DOCUMENT_MESSAGE = "ID Document already exists";
    public static final String INVALID_PHONE_FORMAT_MESSAGE = "Invalid phone number format";
    public static final String INSUFFICIENT_PERMISSIONS_MESSAGE = "You don't have enough permissions";


}
