package com.fulfilment.application.monolith.fulfillment;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ProductWarehouseFulfillment entity.
 * Represents the unique combination of Product, Warehouse, and Store.
 */
public class ProductWarehouseFulfillmentId implements Serializable {

  private Long productId;
  private String warehouseBusinessUnit;
  private Long storeId;

  public ProductWarehouseFulfillmentId() {}

  public ProductWarehouseFulfillmentId(Long productId, String warehouseBusinessUnit, Long storeId) {
    this.productId = productId;
    this.warehouseBusinessUnit = warehouseBusinessUnit;
    this.storeId = storeId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getWarehouseBusinessUnit() {
    return warehouseBusinessUnit;
  }

  public void setWarehouseBusinessUnit(String warehouseBusinessUnit) {
    this.warehouseBusinessUnit = warehouseBusinessUnit;
  }

  public Long getStoreId() {
    return storeId;
  }

  public void setStoreId(Long storeId) {
    this.storeId = storeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProductWarehouseFulfillmentId that = (ProductWarehouseFulfillmentId) o;
    return Objects.equals(productId, that.productId)
        && Objects.equals(warehouseBusinessUnit, that.warehouseBusinessUnit)
        && Objects.equals(storeId, that.storeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, warehouseBusinessUnit, storeId);
  }
}
