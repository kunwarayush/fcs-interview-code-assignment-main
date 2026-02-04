package com.fulfilment.application.monolith.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS ExceptionMapper for BusinessValidationException.
 * Converts business validation errors to HTTP 400 Bad Request responses.
 */
@Provider
public class BusinessValidationExceptionMapper implements ExceptionMapper<BusinessValidationException> {

  @Override
  public Response toResponse(BusinessValidationException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ErrorResponse("VALIDATION_ERROR", exception.getMessage()))
        .build();
  }
}
