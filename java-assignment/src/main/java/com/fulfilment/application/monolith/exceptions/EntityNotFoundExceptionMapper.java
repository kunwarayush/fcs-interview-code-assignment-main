package com.fulfilment.application.monolith.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS ExceptionMapper for EntityNotFoundException.
 * Handles all subclasses (StoreNotFoundException, WarehouseNotFoundException,
 * LocationNotFoundException) with a single mapper - following DRY principle.
 *
 * All entity not found exceptions return HTTP 404.
 */
@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

  @Override
  public Response toResponse(EntityNotFoundException exception) {
    return Response.status(Response.Status.NOT_FOUND)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ErrorResponse("RESOURCE_NOT_FOUND", exception.getMessage()))
        .build();
  }
}
