package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for validating fulfillment operations against business constraints.
 */
@ApplicationScoped
public class FulfillmentValidationService {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject ProductRepository productRepository;

  @Inject WarehouseRepository warehouseRepository;

  @Inject ProductWarehouseFulfillmentRepository fulfillmentRepository;

  /**
   * Validates if a fulfillment association can be created.
   *
   * @param productId the product ID
   * @param warehouseBusinessUnit the warehouse business unit code
   * @param storeId the store ID
   * @throws NotFoundException if any entity doesn't exist
   * @throws BadRequestException if any constraint is violated
   */
  public void validateFulfillmentCreation(
      Long productId, String warehouseBusinessUnit, Long storeId) {

    List<String> violations = new ArrayList<>();

    // Check if entities exist
    if (!productExists(productId)) {
      violations.add("Product with ID " + productId + " does not exist");
    }

    if (!warehouseExists(warehouseBusinessUnit)) {
      violations.add("Warehouse with business unit " + warehouseBusinessUnit + " does not exist");
    }

    if (!storeExists(storeId)) {
      violations.add("Store with ID " + storeId + " does not exist");
    }

    // If entities don't exist, throw exception immediately
    if (!violations.isEmpty()) {
      throw new NotFoundException(String.join("; ", violations));
    }

    // Check if fulfillment already exists
    if (fulfillmentRepository.exists(productId, warehouseBusinessUnit, storeId)) {
      throw new BadRequestException(
          "Fulfillment association already exists for Product "
              + productId
              + ", Warehouse "
              + warehouseBusinessUnit
              + ", and Store "
              + storeId);
    }

    // Validate Constraint 1: Each Product can be fulfilled by max 2 different Warehouses per Store
    long warehouseCountForProduct =
        fulfillmentRepository.countWarehousesForProductInStore(productId, storeId);
    if (warehouseCountForProduct >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      violations.add(
          "Product "
              + productId
              + " already has "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses fulfilling it for Store "
              + storeId
              + ". Maximum allowed is "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE);
    }

    // Validate Constraint 2: Each Store can be fulfilled by max 3 different Warehouses
    // Check if this is a new warehouse for the store
    List<ProductWarehouseFulfillment> storeFullfillments =
        fulfillmentRepository.findByStoreId(storeId);
    boolean isNewWarehouseForStore =
        storeFullfillments.stream()
            .noneMatch(f -> f.getWarehouseBusinessUnit().equals(warehouseBusinessUnit));

    if (isNewWarehouseForStore) {
      long distinctWarehouseCount = fulfillmentRepository.countDistinctWarehousesForStore(storeId);
      if (distinctWarehouseCount >= MAX_WAREHOUSES_PER_STORE) {
        violations.add(
            "Store "
                + storeId
                + " already has "
                + MAX_WAREHOUSES_PER_STORE
                + " different warehouses fulfilling it. Maximum allowed is "
                + MAX_WAREHOUSES_PER_STORE);
      }
    }

    // Validate Constraint 3: Each Warehouse can store max 5 types of Products
    // Check if this is a new product for the warehouse
    List<ProductWarehouseFulfillment> warehouseFullfillments =
        fulfillmentRepository.findByWarehouseBusinessUnit(warehouseBusinessUnit);
    boolean isNewProductForWarehouse =
        warehouseFullfillments.stream().noneMatch(f -> f.getProductId().equals(productId));

    if (isNewProductForWarehouse) {
      long distinctProductCount =
          fulfillmentRepository.countDistinctProductsInWarehouse(warehouseBusinessUnit);
      if (distinctProductCount >= MAX_PRODUCTS_PER_WAREHOUSE) {
        violations.add(
            "Warehouse "
                + warehouseBusinessUnit
                + " already has "
                + MAX_PRODUCTS_PER_WAREHOUSE
                + " different products. Maximum allowed is "
                + MAX_PRODUCTS_PER_WAREHOUSE);
      }
    }

    // If there are any constraint violations, throw exception
    if (!violations.isEmpty()) {
      throw new BadRequestException("Constraint violations: " + String.join("; ", violations));
    }
  }

  /**
   * Validates if a fulfillment association can be deleted.
   *
   * @param productId the product ID
   * @param warehouseBusinessUnit the warehouse business unit code
   * @param storeId the store ID
   * @throws NotFoundException if the fulfillment doesn't exist
   */
  public void validateFulfillmentDeletion(
      Long productId, String warehouseBusinessUnit, Long storeId) {
    if (!fulfillmentRepository.exists(productId, warehouseBusinessUnit, storeId)) {
      throw new NotFoundException(
          "Fulfillment association does not exist for Product "
              + productId
              + ", Warehouse "
              + warehouseBusinessUnit
              + ", and Store "
              + storeId);
    }
  }

  private boolean productExists(Long productId) {
    return productRepository.findByIdOptional(productId).isPresent();
  }

  private boolean warehouseExists(String businessUnitCode) {
    try {
      return warehouseRepository.find("businessUnitCode", businessUnitCode).count() > 0;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean storeExists(Long storeId) {
    return Store.findByIdOptional(storeId).isPresent();
  }
}
