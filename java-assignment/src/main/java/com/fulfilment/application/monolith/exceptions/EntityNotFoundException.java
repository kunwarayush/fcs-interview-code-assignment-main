package com.fulfilment.application.monolith.exceptions;

/**
 * Base exception for all "entity not found" scenarios.
 * Subclasses handle specific entity types (Store, Warehouse, Location, etc.)
 *
 * This is handled by a single ExceptionMapper that returns HTTP 404 for all subclasses.
 */
public abstract class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String message) {
    super(message);
  }
}
