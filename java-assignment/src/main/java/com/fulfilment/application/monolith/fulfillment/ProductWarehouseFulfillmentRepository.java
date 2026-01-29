package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductWarehouseFulfillmentRepository
    implements PanacheRepositoryBase<
        ProductWarehouseFulfillment, ProductWarehouseFulfillmentId> {

  /**
   * Find all fulfillment records for a specific store.
   *
   * @param storeId the store ID
   * @return list of fulfillment records
   */
  public List<ProductWarehouseFulfillment> findByStoreId(Long storeId) {
    return list("storeId", storeId);
  }

  /**
   * Find all fulfillment records for a specific product.
   *
   * @param productId the product ID
   * @return list of fulfillment records
   */
  public List<ProductWarehouseFulfillment> findByProductId(Long productId) {
    return list("productId", productId);
  }

  /**
   * Find all fulfillment records for a specific warehouse.
   *
   * @param warehouseBusinessUnit the warehouse business unit code
   * @return list of fulfillment records
   */
  public List<ProductWarehouseFulfillment> findByWarehouseBusinessUnit(
      String warehouseBusinessUnit) {
    return list("warehouseBusinessUnit", warehouseBusinessUnit);
  }

  /**
   * Count the number of different warehouses fulfilling a product for a specific store.
   * Constraint 1: Each Product can be fulfilled by max 2 different Warehouses per Store
   *
   * @param productId the product ID
   * @param storeId the store ID
   * @return count of warehouses
   */
  public long countWarehousesForProductInStore(Long productId, Long storeId) {
    return count("productId = ?1 and storeId = ?2", productId, storeId);
  }

  /**
   * Count the number of different warehouses fulfilling a specific store. Constraint 2: Each Store
   * can be fulfilled by max 3 different Warehouses
   *
   * @param storeId the store ID
   * @return count of distinct warehouses
   */
  public long countDistinctWarehousesForStore(Long storeId) {
    return find(
            "SELECT COUNT(DISTINCT warehouseBusinessUnit) FROM ProductWarehouseFulfillment WHERE storeId = ?1",
            storeId)
        .project(Long.class)
        .firstResult();
  }

  /**
   * Count the number of different products stored in a specific warehouse. Constraint 3: Each
   * Warehouse can store max 5 types of Products
   *
   * @param warehouseBusinessUnit the warehouse business unit code
   * @return count of distinct products
   */
  public long countDistinctProductsInWarehouse(String warehouseBusinessUnit) {
    return find(
            "SELECT COUNT(DISTINCT productId) FROM ProductWarehouseFulfillment WHERE warehouseBusinessUnit = ?1",
            warehouseBusinessUnit)
        .project(Long.class)
        .firstResult();
  }

  /**
   * Find fulfillment records for a specific product in a specific store.
   *
   * @param productId the product ID
   * @param storeId the store ID
   * @return list of fulfillment records
   */
  public List<ProductWarehouseFulfillment> findByProductIdAndStoreId(Long productId, Long storeId) {
    return list("productId = ?1 and storeId = ?2", productId, storeId);
  }

  /**
   * Check if a fulfillment association already exists.
   *
   * @param productId the product ID
   * @param warehouseBusinessUnit the warehouse business unit code
   * @param storeId the store ID
   * @return true if exists, false otherwise
   */
  public boolean exists(Long productId, String warehouseBusinessUnit, Long storeId) {
    return findByIdOptional(new ProductWarehouseFulfillmentId(productId, warehouseBusinessUnit, storeId))
        .isPresent();
  }

  /**
   * Get the list of warehouse business units that fulfill a product in a specific store.
   *
   * @param productId the product ID
   * @param storeId the store ID
   * @return list of warehouse business unit codes
   */
  public List<String> getWarehousesForProductInStore(Long productId, Long storeId) {
    return find(
            "SELECT warehouseBusinessUnit FROM ProductWarehouseFulfillment WHERE productId = ?1 AND storeId = ?2",
            productId,
            storeId)
        .project(String.class)
        .list();
  }
}
