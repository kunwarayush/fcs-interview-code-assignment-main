package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Junction entity representing the many-to-many relationship between Products, Warehouses, and
 * Stores. This entity tracks which warehouses fulfill which products for specific stores.
 *
 * <p>Business Constraints: 1. Each Product can be fulfilled by max 2 different Warehouses per
 * Store 2. Each Store can be fulfilled by max 3 different Warehouses 3. Each Warehouse can store
 * max 5 types of Products
 */
@Entity
@IdClass(ProductWarehouseFulfillmentId.class)
@Table(
    name = "product_warehouse_fulfillment",
    indexes = {
      @Index(name = "idx_pwf_store", columnList = "storeId"),
      @Index(name = "idx_pwf_product", columnList = "productId"),
      @Index(name = "idx_pwf_warehouse", columnList = "warehouseBusinessUnit")
    })
public class ProductWarehouseFulfillment {

  @Id
  @Column(name = "productId")
  private Long productId;

  @Id
  @Column(name = "warehouseBusinessUnit", length = 255)
  private String warehouseBusinessUnit;

  @Id
  @Column(name = "storeId")
  private Long storeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productId", insertable = false, updatable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "storeId", insertable = false, updatable = false)
  private Store store;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public ProductWarehouseFulfillment() {
    this.createdAt = LocalDateTime.now();
  }

  public ProductWarehouseFulfillment(Long productId, String warehouseBusinessUnit, Long storeId) {
    this.productId = productId;
    this.warehouseBusinessUnit = warehouseBusinessUnit;
    this.storeId = storeId;
    this.createdAt = LocalDateTime.now();
  }

  // Getters and Setters

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

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
