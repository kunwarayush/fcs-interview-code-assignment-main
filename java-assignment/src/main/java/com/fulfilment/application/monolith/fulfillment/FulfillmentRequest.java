package com.fulfilment.application.monolith.fulfillment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating or deleting a fulfillment association.
 */
public class FulfillmentRequest {

  @NotNull(message = "Product ID is required")
  public Long productId;

  @NotBlank(message = "Warehouse business unit is required")
  public String warehouseBusinessUnit;

  @NotNull(message = "Store ID is required")
  public Long storeId;

  public FulfillmentRequest() {}

  public FulfillmentRequest(Long productId, String warehouseBusinessUnit, Long storeId) {
    this.productId = productId;
    this.warehouseBusinessUnit = warehouseBusinessUnit;
    this.storeId = storeId;
  }
}
