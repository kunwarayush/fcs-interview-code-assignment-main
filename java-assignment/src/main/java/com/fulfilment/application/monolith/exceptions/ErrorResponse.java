package com.fulfilment.application.monolith.exceptions;

/**
 * Standard error response structure for API errors.
 * Provides consistent error format across all endpoints.
 */
public class ErrorResponse {

  private final String code;
  private final String message;

  public ErrorResponse(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
