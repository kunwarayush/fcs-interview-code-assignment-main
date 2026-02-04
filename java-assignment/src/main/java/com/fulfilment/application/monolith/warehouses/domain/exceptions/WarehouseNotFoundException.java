package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import com.fulfilment.application.monolith.exceptions.EntityNotFoundException;

/**
 * Exception thrown when a warehouse cannot be found by its business unit code.
 */
public class WarehouseNotFoundException extends EntityNotFoundException {

  private final String businessUnitCode;

  public WarehouseNotFoundException(String businessUnitCode) {
    super("Warehouse with business unit code " + businessUnitCode + " not found");
    this.businessUnitCode = businessUnitCode;
  }

  public String getBusinessUnitCode() {
    return businessUnitCode;
  }
}
