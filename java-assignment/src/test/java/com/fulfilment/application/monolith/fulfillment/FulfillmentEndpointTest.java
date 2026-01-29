package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration tests for the Fulfillment API endpoints.
 *
 * <p>These tests validate: - Creation of fulfillment associations - All three business constraints
 * - Retrieval operations - Deletion operations - Statistics endpoints
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfillmentEndpointTest {

  @Inject ProductWarehouseFulfillmentRepository fulfillmentRepository;

  @Inject ProductRepository productRepository;
  
  @Inject WarehouseRepository warehouseRepository;

  private static Long testProductId1;
  private static Long testProductId2;
  private static Long testProductId3;
  private static Long testProductId4;
  private static Long testProductId5;
  private static Long testProductId6;
  private static Long testProductId7;
  private static Long testProductId8;
  private static Long testProductId9;
  private static Long testStoreId1;
  private static Long testStoreId2;

  private String uniqueSuffix = String.valueOf(System.currentTimeMillis()) + "-EP";

  @BeforeAll
  public static void setupTestData() {
    // Products will be created per test
  }

  @BeforeEach
  @Transactional
  public void setupPerTestData() {
    // Cleanup only fulfillment data
    fulfillmentRepository.deleteAll();
    
    // Create test products with unique names
    Product product1 = new Product("EP-P1-" + uniqueSuffix);
    product1.description = "Test product for fulfillment";
    product1.stock = 100;
    productRepository.persist(product1);
    testProductId1 = product1.id;

    Product product2 = new Product("EP-P2-" + uniqueSuffix);
    product2.description = "Test product for fulfillment";
    product2.stock = 150;
    productRepository.persist(product2);
    testProductId2 = product2.id;

    Product product3 = new Product("EP-P3-" + uniqueSuffix);
    product3.description = "Test product for fulfillment";
    product3.stock = 200;
    productRepository.persist(product3);
    testProductId3 = product3.id;

    Product product4 = new Product("EP-P4-" + uniqueSuffix);
    product4.stock = 100;
    productRepository.persist(product4);
    testProductId4 = product4.id;
    
    Product product5 = new Product("EP-P5-" + uniqueSuffix);
    product5.stock = 100;
    productRepository.persist(product5);
    testProductId5 = product5.id;
    
    Product product6 = new Product("EP-P6-" + uniqueSuffix);
    product6.stock = 100;
    productRepository.persist(product6);
    testProductId6 = product6.id;
    
    Product product7 = new Product("EP-P7-" + uniqueSuffix);
    product7.stock = 100;
    productRepository.persist(product7);
    testProductId7 = product7.id;
    
    Product product8 = new Product("EP-P8-" + uniqueSuffix);
    product8.stock = 100;
    productRepository.persist(product8);
    testProductId8 = product8.id;
    
    Product product9 = new Product("EP-P9-" + uniqueSuffix);
    product9.stock = 100;
    productRepository.persist(product9);
    testProductId9 = product9.id;

    // Create test stores
    Store store1 = new Store("EP-Store-1-" + uniqueSuffix);
    store1.quantityProductsInStock = 500;
    store1.persist();
    testStoreId1 = store1.id;

    Store store2 = new Store("EP-Store-2-" + uniqueSuffix);
    store2.quantityProductsInStock = 600;
    store2.persist();
    testStoreId2 = store2.id;
    
    // Create test warehouses
    DbWarehouse wh1 = new DbWarehouse();
    wh1.businessUnitCode = "WH-TEST-001";
    wh1.location = "Test Location 1";
    wh1.capacity = 1000;
    wh1.stock = 500;
    wh1.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh1);
    
    DbWarehouse wh2 = new DbWarehouse();
    wh2.businessUnitCode = "WH-TEST-002";
    wh2.location = "Test Location 2";
    wh2.capacity = 1500;
    wh2.stock = 700;
    wh2.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh2);
    
    DbWarehouse wh3 = new DbWarehouse();
    wh3.businessUnitCode = "WH-TEST-003";
    wh3.location = "Test Location 3";
    wh3.capacity = 2000;
    wh3.stock = 900;
    wh3.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh3);
    
    DbWarehouse wh4 = new DbWarehouse();
    wh4.businessUnitCode = "WH-TEST-004";
    wh4.location = "Test Location 4";
    wh4.capacity = 2500;
    wh4.stock = 1100;
    wh4.createdAt = LocalDateTime.now();
    warehouseRepository.persist(wh4);
    
    DbWarehouse whConstraint3 = new DbWarehouse();
    whConstraint3.businessUnitCode = "WH-TEST-CONSTRAINT3";
    whConstraint3.location = "Constraint Test Location";
    whConstraint3.capacity = 3000;
    whConstraint3.stock = 1500;
    whConstraint3.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whConstraint3);
    
    DbWarehouse whStats1 = new DbWarehouse();
    whStats1.businessUnitCode = "WH-STATS-001";
    whStats1.location = "Stats Location 1";
    whStats1.capacity = 1000;
    whStats1.stock = 500;
    whStats1.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whStats1);
    
    DbWarehouse whStats2 = new DbWarehouse();
    whStats2.businessUnitCode = "WH-STATS-002";
    whStats2.location = "Stats Location 2";
    whStats2.capacity = 1500;
    whStats2.stock = 700;
    whStats2.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whStats2);
    
    DbWarehouse whStatsTest = new DbWarehouse();
    whStatsTest.businessUnitCode = "WH-STATS-TEST";
    whStatsTest.location = "Stats Test Location";
    whStatsTest.capacity = 2000;
    whStatsTest.stock = 900;
    whStatsTest.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whStatsTest);
    
    DbWarehouse whTestRetrieve = new DbWarehouse();
    whTestRetrieve.businessUnitCode = "WH-TEST-RETRIEVE";
    whTestRetrieve.location = "Retrieve Test Location";
    whTestRetrieve.capacity = 1000;
    whTestRetrieve.stock = 500;
    whTestRetrieve.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whTestRetrieve);
    
    DbWarehouse whTestDelete = new DbWarehouse();
    whTestDelete.businessUnitCode = "WH-TEST-DELETE";
    whTestDelete.location = "Delete Test Location";
    whTestDelete.capacity = 1000;
    whTestDelete.stock = 500;
    whTestDelete.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whTestDelete);
    
    DbWarehouse whAll1 = new DbWarehouse();
    whAll1.businessUnitCode = "WH-ALL-001";
    whAll1.location = "All Test Location 1";
    whAll1.capacity = 1000;
    whAll1.stock = 500;
    whAll1.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whAll1);
    
    DbWarehouse whAll2 = new DbWarehouse();
    whAll2.businessUnitCode = "WH-ALL-002";
    whAll2.location = "All Test Location 2";
    whAll2.capacity = 1000;
    whAll2.stock = 500;
    whAll2.createdAt = LocalDateTime.now();
    warehouseRepository.persist(whAll2);
  }



  @Test
  @Order(1)
  public void testCreateFulfillment_Success() {
    FulfillmentRequest request = new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201)
        .body("productId", equalTo(testProductId1.intValue()))
        .body("warehouseBusinessUnit", equalTo("WH-TEST-001"))
        .body("storeId", equalTo(testStoreId1.intValue()))
        .body("createdAt", notNullValue());
  }

  @Test
  @Order(2)
  public void testCreateFulfillment_ProductNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(99999L, "WH-TEST-001", testStoreId1);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(3)
  public void testCreateFulfillment_StoreNotFound() {
    FulfillmentRequest request = new FulfillmentRequest(testProductId1, "WH-TEST-001", 99999L);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(4)
  public void testCreateFulfillment_DuplicateAssociation() {
    FulfillmentRequest request = new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1);

    // Create first time - should succeed
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    // Create second time - should fail
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(400)
        .body(containsString("already exists"));
  }

  @Test
  @Order(5)
  public void testConstraint1_MaxTwoWarehousesPerProductPerStore() {
    // Create first fulfillment
    FulfillmentRequest request1 = new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1);
    given().contentType(ContentType.JSON).body(request1).when().post("/api/fulfillment").then().statusCode(201);

    // Create second fulfillment
    FulfillmentRequest request2 = new FulfillmentRequest(testProductId1, "WH-TEST-002", testStoreId1);
    given().contentType(ContentType.JSON).body(request2).when().post("/api/fulfillment").then().statusCode(201);

    // Attempt to create third fulfillment - should fail
    FulfillmentRequest request3 = new FulfillmentRequest(testProductId1, "WH-TEST-003", testStoreId1);
    given()
        .contentType(ContentType.JSON)
        .body(request3)
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(400)
        .body(containsString("already has 2 warehouses"));
  }

  @Test
  @Order(6)
  public void testConstraint2_MaxThreeWarehousesPerStore() {
    // Create fulfillments with 3 different warehouses for the same store
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId2, "WH-TEST-002", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId3, "WH-TEST-003", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    // Attempt to add a 4th warehouse - should fail
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-004", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(400)
        .body(containsString("already has 3 different warehouses"));
  }

  @Test
  @Order(7)
  public void testConstraint3_MaxFiveProductsPerWarehouse() {
    // Use pre-created products to avoid transaction issues
    // Create fulfillments with 5 different products for the same warehouse
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId4, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId5, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId6, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId7, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId8, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(201);

    // Attempt to add a 6th product - should fail
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId9, "WH-TEST-CONSTRAINT3", testStoreId1))
        .when()
        .post("/api/fulfillment")
        .then()
        .statusCode(400)
        .body(containsString("already has 5 different products"));
  }

  @Test
  @Order(8)
  public void testGetFulfillmentsByStore() {
    // Create some fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId2, "WH-TEST-002", testStoreId1))
        .post("/api/fulfillment");

    // Retrieve fulfillments for store
    given()
        .when()
        .get("/api/fulfillment/store/" + testStoreId1)
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("[0].storeId", equalTo(testStoreId1.intValue()))
        .body("[1].storeId", equalTo(testStoreId1.intValue()));
  }

  @Test
  @Order(9)
  public void testGetFulfillmentsByProduct() {
    // Create fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-001", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-002", testStoreId2))
        .post("/api/fulfillment");

    // Retrieve fulfillments for product
    given()
        .when()
        .get("/api/fulfillment/product/" + testProductId1)
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("[0].productId", equalTo(testProductId1.intValue()))
        .body("[1].productId", equalTo(testProductId1.intValue()));
  }

  @Test
  @Order(10)
  public void testGetFulfillmentsByWarehouse() {
    // Create fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-TEST-RETRIEVE", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId2, "WH-TEST-RETRIEVE", testStoreId1))
        .post("/api/fulfillment");

    // Retrieve fulfillments for warehouse
    given()
        .when()
        .get("/api/fulfillment/warehouse/WH-TEST-RETRIEVE")
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("[0].warehouseBusinessUnit", equalTo("WH-TEST-RETRIEVE"))
        .body("[1].warehouseBusinessUnit", equalTo("WH-TEST-RETRIEVE"));
  }

  @Test
  @Order(11)
  public void testDeleteFulfillment_Success() {
    FulfillmentRequest request = new FulfillmentRequest(testProductId1, "WH-TEST-DELETE", testStoreId1);

    // Create fulfillment
    given().contentType(ContentType.JSON).body(request).post("/api/fulfillment").then().statusCode(201);

    // Delete fulfillment
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .delete("/api/fulfillment")
        .then()
        .statusCode(204);

    // Verify deletion
    given().when().get("/api/fulfillment/product/" + testProductId1).then().statusCode(200).body("$", hasSize(0));
  }

  @Test
  @Order(12)
  public void testDeleteFulfillment_NotFound() {
    FulfillmentRequest request = new FulfillmentRequest(testProductId1, "WH-NONEXISTENT", testStoreId1);

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .delete("/api/fulfillment")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  public void testGetStoreStats() {
    // Create some fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-STATS-001", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId2, "WH-STATS-002", testStoreId1))
        .post("/api/fulfillment");

    // Get stats
    given()
        .when()
        .get("/api/fulfillment/store/" + testStoreId1 + "/stats")
        .then()
        .statusCode(200)
        .body("entityType", equalTo("Store"))
        .body("entityId", equalTo(testStoreId1.toString()))
        .body("currentCount", equalTo(2))
        .body("maxAllowed", equalTo(3))
        .body("canAddMore", equalTo(true));
  }

  @Test
  @Order(14)
  public void testGetWarehouseStats() {
    // Use pre-created products
    // Create some fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId4, "WH-STATS-TEST", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId5, "WH-STATS-TEST", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId6, "WH-STATS-TEST", testStoreId1))
        .post("/api/fulfillment");

    // Get stats
    given()
        .when()
        .get("/api/fulfillment/warehouse/WH-STATS-TEST/stats")
        .then()
        .statusCode(200)
        .body("entityType", equalTo("Warehouse"))
        .body("entityId", equalTo("WH-STATS-TEST"))
        .body("currentCount", equalTo(3))
        .body("maxAllowed", equalTo(5))
        .body("canAddMore", equalTo(true));
  }

  @Test
  @Order(15)
  public void testGetAllFulfillments() {
    // Create some fulfillments
    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId1, "WH-ALL-001", testStoreId1))
        .post("/api/fulfillment");

    given()
        .contentType(ContentType.JSON)
        .body(new FulfillmentRequest(testProductId2, "WH-ALL-002", testStoreId1))
        .post("/api/fulfillment");

    // Get all fulfillments
    given()
        .when()
        .get("/api/fulfillment")
        .then()
        .statusCode(200)
        .body("$", hasSize(2));
  }
}
