package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.exceptions.EntityNotFoundException;

/**
 * Exception thrown when a location cannot be found by its identifier.
 */
public class LocationNotFoundException extends EntityNotFoundException {

  private final String identifier;

  public LocationNotFoundException(String identifier) {
    super("Location with identifier " + identifier + " not found.");
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }
}
