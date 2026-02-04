package com.fulfilment.application.monolith.exceptions;

/**
 * Centralized error codes for consistent API error responses.
 */
public enum ErrorCode {
  // Not Found errors (404)
  RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found"),
  STORE_NOT_FOUND("STORE_NOT_FOUND", "Store not found"),
  WAREHOUSE_NOT_FOUND("WAREHOUSE_NOT_FOUND", "Warehouse not found"),
  LOCATION_NOT_FOUND("LOCATION_NOT_FOUND", "Location not found"),
  PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found"),

  // Validation errors (400)
  VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed"),
  INVALID_INPUT("INVALID_INPUT", "Invalid input provided"),
  DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource already exists"),
  CAPACITY_EXCEEDED("CAPACITY_EXCEEDED", "Capacity limit exceeded"),
  STOCK_MISMATCH("STOCK_MISMATCH", "Stock values do not match"),

  // Business rule errors (422)
  BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Business rule violation"),
  INVALID_OPERATION("INVALID_OPERATION", "Operation not allowed"),

  // Server errors (500)
  INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

  private final String code;
  private final String defaultMessage;

  ErrorCode(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  public String getCode() {
    return code;
  }

  public String getDefaultMessage() {
    return defaultMessage;
  }
}
