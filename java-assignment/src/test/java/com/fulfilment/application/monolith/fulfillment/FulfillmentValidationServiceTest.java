package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FulfillmentValidationService.
 *
 * <p>Test Categories:
 * 1. Positive Cases - Valid operations that should succeed
 * 2. Negative Cases - Invalid operations with expected failures
 * 3. Edge Cases - Boundary conditions and special scenarios
 * 4. Constraint Validation - All three business constraints
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfillmentValidationServiceTest {

  @Inject FulfillmentValidationService validationService;

  @Inject ProductWarehouseFulfillmentRepository fulfillmentRepository;

  @Inject ProductRepository productRepository;

  @Inject WarehouseRepository warehouseRepository;

  private String uniqueSuffix = String.valueOf(System.currentTimeMillis());
  
  private static Long testProductId1;
  private static Long testProductId2;
  private static Long testProductId3;
  private static Long testProductId4;
  private static Long testProductId5;
  private static Long testProductId6;
  private static Long testStoreId1;
  private static Long testStoreId2;
  private static String testWarehouse1 = "WH-VAL-001";
  private static String testWarehouse2 = "WH-VAL-002";
  private static String testWarehouse3 = "WH-VAL-003";
  private static String testWarehouse4 = "WH-VAL-004";

  @BeforeAll
  public static void setupTestData() {
    // Note: Product entities are set up via import.sql or created dynamically in tests
    // These IDs will be set in @BeforeEach using existing products
  }

  @BeforeEach
  @Transactional
  public void setupPerTestData() {
    // Clean up only fulfillment data (don't delete products/warehouses to avoid breaking other tests)
    fulfillmentRepository.deleteAll();
    
    // Create fresh test products with unique names
    Product p1 = new Product("Val-P1-" + uniqueSuffix);
    p1.stock = 100;
    productRepository.persist(p1);
    testProductId1 = p1.id;

    Product p2 = new Product("Val-P2-" + uniqueSuffix);
    p2.stock = 150;
    productRepository.persist(p2);
    testProductId2 = p2.id;

    Product p3 = new Product("Val-P3-" + uniqueSuffix);
    p3.stock = 200;
    productRepository.persist(p3);
    testProductId3 = p3.id;

    Product p4 = new Product("Val-P4-" + uniqueSuffix);
    p4.stock = 250;
    productRepository.persist(p4);
    testProductId4 = p4.id;

    Product p5 = new Product("Val-P5-" + uniqueSuffix);
    p5.stock = 300;
    productRepository.persist(p5);
    testProductId5 = p5.id;

    Product p6 = new Product("Val-P6-" + uniqueSuffix);
    p6.stock = 350;
    productRepository.persist(p6);
    testProductId6 = p6.id;

    // Create test stores
    Store store1 = new Store("Val-Store-1-" + uniqueSuffix);
    store1.quantityProductsInStock = 500;
    store1.persist();
    testStoreId1 = store1.id;

    Store store2 = new Store("Val-Store-2-" + uniqueSuffix);
    store2.quantityProductsInStock = 600;
    store2.persist();
    testStoreId2 = store2.id;
    
    // Create test warehouses
    DbWarehouse wh1 = new DbWarehouse();
    wh1.businessUnitCode = testWarehouse1;
    wh1.location = "Location 1";
    wh1.capacity = 1000;
    wh1.stock = 500;
    wh1.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh1);

    DbWarehouse wh2 = new DbWarehouse();
    wh2.businessUnitCode = testWarehouse2;
    wh2.location = "Location 2";
    wh2.capacity = 1500;
    wh2.stock = 700;
    wh2.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh2);

    DbWarehouse wh3 = new DbWarehouse();
    wh3.businessUnitCode = testWarehouse3;
    wh3.location = "Location 3";
    wh3.capacity = 2000;
    wh3.stock = 900;
    wh3.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh3);

    DbWarehouse wh4 = new DbWarehouse();
    wh4.businessUnitCode = testWarehouse4;
    wh4.location = "Location 4";
    wh4.capacity = 2500;
    wh4.stock = 1100;
    wh4.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh4);
  }



  // ========================================================================
  // POSITIVE TEST CASES - Valid scenarios that should succeed
  // ========================================================================

  @Test
  @Order(1)
  @DisplayName("Positive: Validate creation of first fulfillment association")
  public void testValidateCreation_FirstAssociation_Success() {
    // Given: No existing fulfillment associations
    // When: Validating a new fulfillment
    // Then: Should pass without exceptions
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse1, testStoreId1);
    });
  }

  @Test
  @Order(2)
  @DisplayName("Positive: Validate creation with existing unrelated fulfillments")
  @Transactional
  public void testValidateCreation_WithUnrelatedFulfillments_Success() {
    // Given: Existing fulfillments for different product/store/warehouse combinations
    ProductWarehouseFulfillment existing1 = 
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1);
    fulfillmentRepository.persist(existing1);

    // When: Validating a new different combination
    // Then: Should pass
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId2, testWarehouse2, testStoreId2);
    });
  }

  @Test
  @Order(3)
  @DisplayName("Positive: Validate second warehouse for same product in same store")
  @Transactional
  public void testValidateCreation_SecondWarehouse_Success() {
    // Given: One warehouse already fulfilling product in store
    ProductWarehouseFulfillment existing = 
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1);
    fulfillmentRepository.persist(existing);

    // When: Adding second warehouse for same product/store
    // Then: Should pass (max is 2)
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse2, testStoreId1);
    });
  }

  @Test
  @Order(4)
  @DisplayName("Positive: Validate deletion of existing fulfillment")
  @Transactional
  public void testValidateDeletion_ExistingFulfillment_Success() {
    // Given: An existing fulfillment association
    ProductWarehouseFulfillment existing = 
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1);
    fulfillmentRepository.persist(existing);

    // When: Validating deletion
    // Then: Should pass
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentDeletion(testProductId1, testWarehouse1, testStoreId1);
    });
  }

  // ========================================================================
  // NEGATIVE TEST CASES - Invalid scenarios that should fail
  // ========================================================================

  @Test
  @Order(10)
  @DisplayName("Negative: Product does not exist")
  public void testValidateCreation_ProductNotFound_ThrowsNotFoundException() {
    // Given: A non-existent product ID
    Long nonExistentProductId = 999999L;

    // When: Validating creation
    // Then: Should throw NotFoundException
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      validationService.validateFulfillmentCreation(
          nonExistentProductId, testWarehouse1, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("Product"));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(11)
  @DisplayName("Negative: Store does not exist")
  public void testValidateCreation_StoreNotFound_ThrowsNotFoundException() {
    // Given: A non-existent store ID
    Long nonExistentStoreId = 999999L;

    // When: Validating creation
    // Then: Should throw NotFoundException
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      validationService.validateFulfillmentCreation(
          testProductId1, testWarehouse1, nonExistentStoreId);
    });

    assertTrue(exception.getMessage().contains("Store"));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(12)
  @DisplayName("Negative: Warehouse does not exist")
  public void testValidateCreation_WarehouseNotFound_ThrowsNotFoundException() {
    // Given: A non-existent warehouse business unit
    String nonExistentWarehouse = "WH-NONEXISTENT-999";

    // When: Validating creation
    // Then: Should throw NotFoundException
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      validationService.validateFulfillmentCreation(
          testProductId1, nonExistentWarehouse, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("Warehouse"));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @Order(13)
  @DisplayName("Negative: Duplicate fulfillment association")
  @Transactional
  public void testValidateCreation_DuplicateAssociation_ThrowsBadRequestException() {
    // Given: An existing fulfillment association
    ProductWarehouseFulfillment existing = 
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1);
    fulfillmentRepository.persist(existing);

    // When: Attempting to create the same association
    // Then: Should throw BadRequestException
    BadRequestException exception = assertThrows(BadRequestException.class, () -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse1, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  @Order(14)
  @DisplayName("Negative: Delete non-existent fulfillment")
  public void testValidateDeletion_NonExistentFulfillment_ThrowsNotFoundException() {
    // Given: No fulfillment associations exist
    // When: Validating deletion
    // Then: Should throw NotFoundException
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      validationService.validateFulfillmentDeletion(testProductId1, testWarehouse1, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("does not exist"));
  }

  // ========================================================================
  // CONSTRAINT VALIDATION TESTS
  // ========================================================================

  @Test
  @Order(20)
  @DisplayName("Constraint 1: Exceeds max 2 warehouses per product per store")
  @Transactional
  public void testConstraint1_ExceedsMaxWarehousesPerProductPerStore_ThrowsBadRequestException() {
    // Given: Product already has 2 warehouses for a store
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse2, testStoreId1));

    // When: Attempting to add a third warehouse
    // Then: Should throw BadRequestException with constraint message
    BadRequestException exception = assertThrows(BadRequestException.class, () -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse3, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("already has 2 warehouses"));
    assertTrue(exception.getMessage().contains("Maximum allowed is 2"));
  }

  @Test
  @Order(21)
  @DisplayName("Constraint 1: Same product in different stores - should allow")
  @Transactional
  public void testConstraint1_SameProductDifferentStores_Success() {
    // Given: Product has 2 warehouses in Store1
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse2, testStoreId1));

    // When: Adding same product to different store
    // Then: Should succeed (constraint is per store)
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse1, testStoreId2);
    });
  }

  @Test
  @Order(22)
  @DisplayName("Constraint 2: Exceeds max 3 warehouses per store")
  @Transactional
  public void testConstraint2_ExceedsMaxWarehousesPerStore_ThrowsBadRequestException() {
    // Given: Store already has 3 different warehouses
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse2, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId3, testWarehouse3, testStoreId1));

    // When: Attempting to add a fourth warehouse
    // Then: Should throw BadRequestException
    BadRequestException exception = assertThrows(BadRequestException.class, () -> {
      validationService.validateFulfillmentCreation(testProductId4, testWarehouse4, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("already has 3 different warehouses"));
    assertTrue(exception.getMessage().contains("Maximum allowed is 3"));
  }

  @Test
  @Order(23)
  @DisplayName("Constraint 2: Adding product to existing warehouse in store - should allow")
  @Transactional
  public void testConstraint2_ExistingWarehouseNewProduct_Success() {
    // Given: Store has 3 warehouses
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse2, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId3, testWarehouse3, testStoreId1));

    // When: Adding another product to an already-used warehouse
    // Then: Should succeed (not adding a new warehouse)
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId4, testWarehouse1, testStoreId1);
    });
  }

  @Test
  @Order(24)
  @DisplayName("Constraint 3: Exceeds max 5 products per warehouse")
  @Transactional
  public void testConstraint3_ExceedsMaxProductsPerWarehouse_ThrowsBadRequestException() {
    // Given: Warehouse already has 5 different products
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId3, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId4, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId5, testWarehouse1, testStoreId1));

    // When: Attempting to add a sixth product
    // Then: Should throw BadRequestException
    BadRequestException exception = assertThrows(BadRequestException.class, () -> {
      validationService.validateFulfillmentCreation(testProductId6, testWarehouse1, testStoreId1);
    });

    assertTrue(exception.getMessage().contains("already has 5 different products"));
    assertTrue(exception.getMessage().contains("Maximum allowed is 5"));
  }

  @Test
  @Order(25)
  @DisplayName("Constraint 3: Adding same product to warehouse in different store - should allow")
  @Transactional
  public void testConstraint3_SameProductDifferentStore_Success() {
    // Given: Warehouse has 5 products in Store1
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId3, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId4, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId5, testWarehouse1, testStoreId1));

    // When: Adding same product to warehouse in different store
    // Then: Should succeed (product already counted)
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse1, testStoreId2);
    });
  }

  // ========================================================================
  // EDGE CASES AND BOUNDARY CONDITIONS
  // ========================================================================

  @Test
  @Order(30)
  @DisplayName("Edge Case: Exactly at constraint limit - Constraint 1")
  @Transactional
  public void testEdgeCase_ExactlyTwoWarehouses_Success() {
    // Given: Product has 1 warehouse
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));

    // When: Adding exactly the second warehouse (at limit)
    // Then: Should succeed
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse2, testStoreId1);
    });
  }

  @Test
  @Order(31)
  @DisplayName("Edge Case: Exactly at constraint limit - Constraint 2")
  @Transactional
  public void testEdgeCase_ExactlyThreeWarehouses_Success() {
    // Given: Store has 2 warehouses
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse2, testStoreId1));

    // When: Adding exactly the third warehouse (at limit)
    // Then: Should succeed
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId3, testWarehouse3, testStoreId1);
    });
  }

  @Test
  @Order(32)
  @DisplayName("Edge Case: Exactly at constraint limit - Constraint 3")
  @Transactional
  public void testEdgeCase_ExactlyFiveProducts_Success() {
    // Given: Warehouse has 4 products
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId3, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId4, testWarehouse1, testStoreId1));

    // When: Adding exactly the fifth product (at limit)
    // Then: Should succeed
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId5, testWarehouse1, testStoreId1);
    });
  }

  @Test
  @Order(33)
  @DisplayName("Edge Case: Multiple constraints combined")
  @Transactional
  public void testEdgeCase_MultipleConstraintsCombined_Success() {
    // Given: Complex scenario with multiple constraints active
    // Store1 has 2 warehouses, Product1 has 1 warehouse in Store1
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId1, testWarehouse1, testStoreId1));
    fulfillmentRepository.persist(
        new ProductWarehouseFulfillment(testProductId2, testWarehouse2, testStoreId1));

    // When: Adding Product1 to Warehouse2 in Store1
    // Then: Should succeed (within all constraints)
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse2, testStoreId1);
    });
  }

  @Test
  @Order(34)
  @DisplayName("Edge Case: All entities null should throw exception")
  public void testEdgeCase_NullEntities_ThrowsException() {
    // When: All parameters are for non-existent entities
    // Then: Should throw NotFoundException with multiple violations
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      validationService.validateFulfillmentCreation(999999L, "WH-NULL", 999999L);
    });

    // Should mention multiple missing entities
    String message = exception.getMessage();
    assertTrue(message.contains("Product") || message.contains("Store") || message.contains("Warehouse"));
  }

  @Test
  @Order(35)
  @DisplayName("Edge Case: Empty database - first fulfillment creation")
  public void testEdgeCase_EmptyDatabase_FirstFulfillment_Success() {
    // Given: No fulfillments exist (cleanup already done)
    assertEquals(0, fulfillmentRepository.count());

    // When: Creating first ever fulfillment
    // Then: Should succeed
    assertDoesNotThrow(() -> {
      validationService.validateFulfillmentCreation(testProductId1, testWarehouse1, testStoreId1);
    });
  }
}
