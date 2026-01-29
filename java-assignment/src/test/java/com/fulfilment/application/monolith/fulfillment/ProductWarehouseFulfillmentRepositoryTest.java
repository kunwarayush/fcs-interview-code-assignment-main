package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductWarehouseFulfillmentRepository.
 *
 * <p>Tests repository methods for:
 * - CRUD operations
 * - Query methods (findByStoreId, findByProductId, etc.)
 * - Constraint checking methods (count methods)
 * - Edge cases and boundary conditions
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductWarehouseFulfillmentRepositoryTest {

  @Inject ProductWarehouseFulfillmentRepository repository;

  private static Long testProductId1;
  private static Long testProductId2;
  private static Long testProductId3;
  private static Long testStoreId1;
  private static Long testStoreId2;

  @Inject ProductRepository productRepository;

  private String uniqueSuffix = String.valueOf(System.currentTimeMillis()) + "-R";

  @BeforeAll
  public static void setupTestData() {
    // Products will be created in @BeforeEach
  }

  @BeforeEach
  @Transactional
  public void setupPerTestData() {
    // Clean up only fulfillment data
    repository.deleteAll();
    
    // Create test products with unique names
    Product p1 = new Product("Repo-P1-" + uniqueSuffix);
    p1.stock = 100;
    productRepository.persist(p1);
    testProductId1 = p1.id;

    Product p2 = new Product("Repo-P2-" + uniqueSuffix);
    p2.stock = 150;
    productRepository.persist(p2);
    testProductId2 = p2.id;

    Product p3 = new Product("Repo-P3-" + uniqueSuffix);
    p3.stock = 200;
    productRepository.persist(p3);
    testProductId3 = p3.id;

    // Create test stores
    Store store1 = new Store("Repo-Store-1-" + uniqueSuffix);
    store1.quantityProductsInStock = 500;
    store1.persist();
    testStoreId1 = store1.id;

    Store store2 = new Store("Repo-Store-2-" + uniqueSuffix);
    store2.quantityProductsInStock = 600;
    store2.persist();
    testStoreId2 = store2.id;
  }



  // ========================================================================
  // CRUD OPERATIONS TESTS
  // ========================================================================

  @Test
  @Order(1)
  @DisplayName("Create: Persist new fulfillment")
  @Transactional
  public void testPersist_NewFulfillment_Success() {
    // Given: A new fulfillment object
    ProductWarehouseFulfillment fulfillment = 
        new ProductWarehouseFulfillment(testProductId1, "WH-CREATE-001", testStoreId1);

    // When: Persisting it
    repository.persist(fulfillment);

    // Then: Should be retrievable
    assertTrue(repository.exists(testProductId1, "WH-CREATE-001", testStoreId1));
    assertEquals(1, repository.count());
  }

  @Test
  @Order(2)
  @DisplayName("Read: Find by composite ID")
  @Transactional
  public void testFindById_ExistingFulfillment_ReturnsEntity() {
    // Given: An existing fulfillment
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-READ-001", testStoreId1));

    // When: Finding by composite ID
    ProductWarehouseFulfillmentId id = 
        new ProductWarehouseFulfillmentId(testProductId1, "WH-READ-001", testStoreId1);
    var result = repository.findByIdOptional(id);

    // Then: Should be found
    assertTrue(result.isPresent());
    assertEquals(testProductId1, result.get().getProductId());
    assertEquals("WH-READ-001", result.get().getWarehouseBusinessUnit());
    assertEquals(testStoreId1, result.get().getStoreId());
  }

  @Test
  @Order(3)
  @DisplayName("Delete: Remove by composite ID")
  @Transactional
  public void testDeleteById_ExistingFulfillment_Success() {
    // Given: An existing fulfillment
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-DELETE-001", testStoreId1));
    assertEquals(1, repository.count());

    // When: Deleting by ID
    ProductWarehouseFulfillmentId id = 
        new ProductWarehouseFulfillmentId(testProductId1, "WH-DELETE-001", testStoreId1);
    repository.deleteById(id);

    // Then: Should be deleted
    assertFalse(repository.exists(testProductId1, "WH-DELETE-001", testStoreId1));
    assertEquals(0, repository.count());
  }

  // ========================================================================
  // QUERY METHODS TESTS
  // ========================================================================

  @Test
  @Order(10)
  @DisplayName("Query: Find by store ID")
  @Transactional
  public void testFindByStoreId_MultipleResults_ReturnsAll() {
    // Given: Multiple fulfillments for same store
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-QUERY-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId2, "WH-QUERY-002", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId3, "WH-QUERY-003", testStoreId2));

    // When: Finding by store ID
    List<ProductWarehouseFulfillment> results = repository.findByStoreId(testStoreId1);

    // Then: Should return all matching records
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(f -> f.getStoreId().equals(testStoreId1)));
  }

  @Test
  @Order(11)
  @DisplayName("Query: Find by product ID")
  @Transactional
  public void testFindByProductId_MultipleResults_ReturnsAll() {
    // Given: Multiple fulfillments for same product
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-QUERY-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-QUERY-002", testStoreId2));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId2, "WH-QUERY-003", testStoreId1));

    // When: Finding by product ID
    List<ProductWarehouseFulfillment> results = repository.findByProductId(testProductId1);

    // Then: Should return all matching records
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(f -> f.getProductId().equals(testProductId1)));
  }

  @Test
  @Order(12)
  @DisplayName("Query: Find by warehouse business unit")
  @Transactional
  public void testFindByWarehouseBusinessUnit_MultipleResults_ReturnsAll() {
    // Given: Multiple fulfillments for same warehouse
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-QUERY-SAME", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId2, "WH-QUERY-SAME", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId3, "WH-QUERY-DIFF", testStoreId1));

    // When: Finding by warehouse
    List<ProductWarehouseFulfillment> results = 
        repository.findByWarehouseBusinessUnit("WH-QUERY-SAME");

    // Then: Should return all matching records
    assertEquals(2, results.size());
    assertTrue(results.stream()
        .allMatch(f -> f.getWarehouseBusinessUnit().equals("WH-QUERY-SAME")));
  }

  @Test
  @Order(13)
  @DisplayName("Query: Find by product and store")
  @Transactional
  public void testFindByProductIdAndStoreId_ReturnsMatchingRecords() {
    // Given: Multiple fulfillments
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-002", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-003", testStoreId2));

    // When: Finding by product and store
    List<ProductWarehouseFulfillment> results = 
        repository.findByProductIdAndStoreId(testProductId1, testStoreId1);

    // Then: Should return matching records
    assertEquals(2, results.size());
    assertTrue(results.stream()
        .allMatch(f -> f.getProductId().equals(testProductId1) 
                    && f.getStoreId().equals(testStoreId1)));
  }

  @Test
  @Order(14)
  @DisplayName("Query: Get warehouses for product in store")
  @Transactional
  public void testGetWarehousesForProductInStore_ReturnsWarehouseList() {
    // Given: Product served by multiple warehouses in one store
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-LIST-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-LIST-002", testStoreId1));

    // When: Getting warehouses
    List<String> warehouses = 
        repository.getWarehousesForProductInStore(testProductId1, testStoreId1);

    // Then: Should return list of warehouse codes
    assertEquals(2, warehouses.size());
    assertTrue(warehouses.contains("WH-LIST-001"));
    assertTrue(warehouses.contains("WH-LIST-002"));
  }

  // ========================================================================
  // COUNTING METHODS FOR CONSTRAINTS
  // ========================================================================

  @Test
  @Order(20)
  @DisplayName("Count: Warehouses for product in store")
  @Transactional
  public void testCountWarehousesForProductInStore_ReturnsCorrectCount() {
    // Given: Product has 2 warehouses in store1, 1 in store2
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-COUNT-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-COUNT-002", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-COUNT-003", testStoreId2));

    // When: Counting for store1
    long count = repository.countWarehousesForProductInStore(testProductId1, testStoreId1);

    // Then: Should return 2
    assertEquals(2, count);
  }

  @Test
  @Order(21)
  @DisplayName("Count: Distinct warehouses for store")
  @Transactional
  public void testCountDistinctWarehousesForStore_ReturnsCorrectCount() {
    // Given: Store has fulfillments from 3 different warehouses
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-DISTINCT-001", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId2, "WH-DISTINCT-002", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId3, "WH-DISTINCT-003", testStoreId1));
    // Same warehouse again - should not be counted twice
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-DISTINCT-001", testStoreId2));

    // When: Counting distinct warehouses for store1
    long count = repository.countDistinctWarehousesForStore(testStoreId1);

    // Then: Should return 3 (distinct count)
    assertEquals(3, count);
  }

  @Test
  @Order(22)
  @DisplayName("Count: Distinct products in warehouse")
  @Transactional
  public void testCountDistinctProductsInWarehouse_ReturnsCorrectCount() {
    // Given: Warehouse has 3 different products
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-PROD-COUNT", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId2, "WH-PROD-COUNT", testStoreId1));
    repository.persist(
        new ProductWarehouseFulfillment(testProductId3, "WH-PROD-COUNT", testStoreId1));
    // Same product in different store - should not be counted twice
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-PROD-COUNT", testStoreId2));

    // When: Counting distinct products
    long count = repository.countDistinctProductsInWarehouse("WH-PROD-COUNT");

    // Then: Should return 3 (distinct count)
    assertEquals(3, count);
  }

  // ========================================================================
  // EXISTS METHOD TESTS
  // ========================================================================

  @Test
  @Order(30)
  @DisplayName("Exists: Returns true for existing fulfillment")
  @Transactional
  public void testExists_ExistingFulfillment_ReturnsTrue() {
    // Given: An existing fulfillment
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-EXISTS", testStoreId1));

    // When: Checking existence
    boolean exists = repository.exists(testProductId1, "WH-EXISTS", testStoreId1);

    // Then: Should return true
    assertTrue(exists);
  }

  @Test
  @Order(31)
  @DisplayName("Exists: Returns false for non-existing fulfillment")
  public void testExists_NonExistingFulfillment_ReturnsFalse() {
    // When: Checking for non-existent fulfillment
    boolean exists = repository.exists(testProductId1, "WH-NONEXISTENT", testStoreId1);

    // Then: Should return false
    assertFalse(exists);
  }

  // ========================================================================
  // EDGE CASES AND BOUNDARY CONDITIONS
  // ========================================================================

  @Test
  @Order(40)
  @DisplayName("Edge: Empty result when no matches")
  @Transactional
  public void testFindByStoreId_NoMatches_ReturnsEmptyList() {
    // Given: Fulfillments exist but not for the queried store
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-EDGE", testStoreId1));

    // When: Querying for different store
    List<ProductWarehouseFulfillment> results = repository.findByStoreId(testStoreId2);

    // Then: Should return empty list
    assertTrue(results.isEmpty());
  }

  @Test
  @Order(41)
  @DisplayName("Edge: Count returns zero when no matches")
  public void testCountWarehousesForProductInStore_NoMatches_ReturnsZero() {
    // When: Counting for product/store with no fulfillments
    long count = repository.countWarehousesForProductInStore(testProductId1, testStoreId1);

    // Then: Should return 0
    assertEquals(0, count);
  }

  @Test
  @Order(42)
  @DisplayName("Edge: Composite key uniqueness enforcement")
  @Transactional
  public void testCompositePrimaryKey_DuplicateInsertion_ThrowsException() {
    // Given: An existing fulfillment
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-UNIQUE", testStoreId1));
    repository.flush();

    // When: Attempting to insert duplicate
    ProductWarehouseFulfillment duplicate = 
        new ProductWarehouseFulfillment(testProductId1, "WH-UNIQUE", testStoreId1);

    // Then: Should throw exception on flush
    assertThrows(Exception.class, () -> {
      repository.persist(duplicate);
      repository.flush();
    });
  }

  @Test
  @Order(43)
  @DisplayName("Edge: Case sensitivity in warehouse business unit")
  @Transactional
  public void testWarehouseBusinessUnit_CaseSensitive() {
    // Given: Fulfillment with specific case
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, "WH-case-test", testStoreId1));

    // When: Searching with different case
    boolean existsLower = repository.exists(testProductId1, "wh-case-test", testStoreId1);
    boolean existsCorrect = repository.exists(testProductId1, "WH-case-test", testStoreId1);

    // Then: Should be case-sensitive
    assertFalse(existsLower);
    assertTrue(existsCorrect);
  }

  @Test
  @Order(44)
  @DisplayName("Edge: Very large warehouse business unit code")
  @Transactional
  public void testWarehouseBusinessUnit_LongString_Success() {
    // Given: Very long warehouse code (within VARCHAR(255) limit)
    String longCode = "WH-" + "A".repeat(200);
    
    // When: Persisting with long code
    repository.persist(
        new ProductWarehouseFulfillment(testProductId1, longCode, testStoreId1));

    // Then: Should succeed
    assertTrue(repository.exists(testProductId1, longCode, testStoreId1));
  }

  @Test
  @Order(45)
  @DisplayName("Edge: CreatedAt timestamp is set automatically")
  @Transactional
  public void testCreatedAt_AutomaticallySet() {
    // Given: New fulfillment
    ProductWarehouseFulfillment fulfillment = 
        new ProductWarehouseFulfillment(testProductId1, "WH-TIMESTAMP", testStoreId1);

    // When: Persisting
    repository.persist(fulfillment);

    // Then: CreatedAt should be set
    assertNotNull(fulfillment.getCreatedAt());
  }
}
