package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import com.fulfilment.application.monolith.exceptions.EntityNotFoundException;

/**
 * Exception thrown when a warehouse cannot be found by its business unit code or ID.
 */
public class WarehouseNotFoundException extends EntityNotFoundException {

  private final String identifier;

  public WarehouseNotFoundException(String businessUnitCode) {
    super("Warehouse with business unit code " + businessUnitCode + " not found");
    this.identifier = businessUnitCode;
  }

  public WarehouseNotFoundException(Long id) {
    super("Warehouse with ID " + id + " not found");
    this.identifier = String.valueOf(id);
  }

  public String getIdentifier() {
    return identifier;
  }
}
