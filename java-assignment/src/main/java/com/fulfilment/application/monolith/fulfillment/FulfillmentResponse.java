package com.fulfilment.application.monolith.fulfillment;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for fulfillment response.
 */
public class FulfillmentResponse {

  public Long productId;
  public String productName;
  public String warehouseBusinessUnit;
  public Long storeId;
  public String storeName;
  public LocalDateTime createdAt;

  public FulfillmentResponse() {}

  public FulfillmentResponse(
      Long productId,
      String productName,
      String warehouseBusinessUnit,
      Long storeId,
      String storeName,
      LocalDateTime createdAt) {
    this.productId = productId;
    this.productName = productName;
    this.warehouseBusinessUnit = warehouseBusinessUnit;
    this.storeId = storeId;
    this.storeName = storeName;
    this.createdAt = createdAt;
  }

  public static FulfillmentResponse from(ProductWarehouseFulfillment fulfillment) {
    return new FulfillmentResponse(
        fulfillment.getProductId(),
        fulfillment.getProduct() != null ? fulfillment.getProduct().name : null,
        fulfillment.getWarehouseBusinessUnit(),
        fulfillment.getStoreId(),
        fulfillment.getStore() != null ? fulfillment.getStore().name : null,
        fulfillment.getCreatedAt());
  }
}
