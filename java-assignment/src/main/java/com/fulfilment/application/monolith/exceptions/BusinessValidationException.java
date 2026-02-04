package com.fulfilment.application.monolith.exceptions;

/**
 * Exception for business validation failures.
 * Use instead of IllegalArgumentException for domain-specific validation errors.
 * Results in HTTP 400 Bad Request.
 */
public class BusinessValidationException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessValidationException(String message) {
    super(message);
    this.errorCode = ErrorCode.VALIDATION_ERROR;
  }

  public BusinessValidationException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
