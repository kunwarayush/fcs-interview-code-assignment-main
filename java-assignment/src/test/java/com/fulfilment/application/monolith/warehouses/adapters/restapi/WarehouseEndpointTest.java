package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

/**
 * Integration tests for Warehouse REST API endpoints
 * Tests all CRUD operations with proper validations
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseEndpointTest {

  private static final String WAREHOUSE_ENDPOINT = "/warehouse";
  private static final String TEST_BUSINESS_UNIT = "TEST-WH-" + System.currentTimeMillis();
  private static final String TEST_BUSINESS_UNIT_2 = "TEST-WH2-" + System.currentTimeMillis();
  private static String createdWarehouseId;

  @Test
  @Order(1)
  @DisplayName("Should create a new warehouse with valid data")
  void testCreateWarehouse_Success() {
    String requestBody =
        "{"
            + "\"businessUnitCode\": \""
            + TEST_BUSINESS_UNIT
            + "\","
            + "\"location\": \"AMSTERDAM-001\","
            + "\"capacity\": 50,"
            + "\"stock\": 10"
            + "}";

    Response response = given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200)
        .body("businessUnitCode", is(TEST_BUSINESS_UNIT))
        .body("location", is("AMSTERDAM-001"))
        .body("capacity", is(50))
        .body("stock", is(10))
        .body("id", notNullValue())
        .extract()
        .response();

    // Store the ID for subsequent tests
    createdWarehouseId = response.jsonPath().getString("id");
  }

  @Test
  @Order(2)
  @DisplayName("Should get warehouse by ID")
  void testGetWarehouse_Success() {
    given()
        .when()
        .get(WAREHOUSE_ENDPOINT + "/" + createdWarehouseId)
        .then()
        .statusCode(200)
        .body("id", is(createdWarehouseId))
        .body("businessUnitCode", is(TEST_BUSINESS_UNIT))
        .body("location", is("AMSTERDAM-001"))
        .body("capacity", is(50))
        .body("stock", is(10));
  }

  @Test
  @Order(3)
  @DisplayName("Should fail to create warehouse with duplicate business unit code")
  void testCreateWarehouse_DuplicateCode() {
    String requestBody =
        "{"
            + "\"businessUnitCode\": \""
            + TEST_BUSINESS_UNIT
            + "\","
            + "\"location\": \"ZWOLLE-001\","
            + "\"capacity\": 30,"
            + "\"stock\": 5"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(400)
        .body(containsString("already exists"));
  }

  @Test
  @Order(4)
  @DisplayName("Should fail to create warehouse with invalid location")
  void testCreateWarehouse_InvalidLocation() {
    String requestBody =
        "{"
            + "\"businessUnitCode\": \"INVALID-LOC-"
            + System.currentTimeMillis()
            + "\","
            + "\"location\": \"NONEXISTENT-999\","
            + "\"capacity\": 30,"
            + "\"stock\": 5"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(404)
        .body(containsString("not found"));
  }

  @Test
  @Order(5)
  @DisplayName("Should fail when stock exceeds capacity")
  void testCreateWarehouse_StockExceedsCapacity() {
    String requestBody =
        "{"
            + "\"businessUnitCode\": \"STOCK-EXCEED-"
            + System.currentTimeMillis()
            + "\","
            + "\"location\": \"AMSTERDAM-002\","
            + "\"capacity\": 10,"
            + "\"stock\": 50"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(400)
        .body(containsString("exceeds capacity"));
  }

  @Test
  @Order(6)
  @DisplayName("Should fail when location max capacity exceeded")
  void testCreateWarehouse_LocationCapacityExceeded() {
    // AMSTERDAM-002 has maxCapacity of 75, maxNumberOfWarehouses of 3
    // Create first warehouse with capacity 50
    String buCode1 = "CAP-TEST-A-" + System.currentTimeMillis();
    String requestBody1 =
        "{"
            + "\"businessUnitCode\": \""
            + buCode1
            + "\","
            + "\"location\": \"AMSTERDAM-002\","
            + "\"capacity\": 50,"
            + "\"stock\": 5"
            + "}";

    given().contentType(ContentType.JSON).body(requestBody1).when().post(WAREHOUSE_ENDPOINT);

    // Try to create second warehouse with capacity 30 (total would be 80 > 75)
    String buCode2 = "CAP-TEST-B-" + System.currentTimeMillis();
    String requestBody2 =
        "{"
            + "\"businessUnitCode\": \""
            + buCode2
            + "\","
            + "\"location\": \"AMSTERDAM-002\","
            + "\"capacity\": 30,"
            + "\"stock\": 3"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody2)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(400)
        .body(containsString("maximum capacity"));
  }

  @Test
  @Order(7)
  @DisplayName("Should fail when max warehouses at location reached")
  void testCreateWarehouse_MaxWarehousesReached() {
    // HELMOND-001 has maxNumberOfWarehouses of 1
    String buCode1 = "HELMOND-1-" + System.currentTimeMillis();
    String requestBody1 =
        "{"
            + "\"businessUnitCode\": \""
            + buCode1
            + "\","
            + "\"location\": \"HELMOND-001\","
            + "\"capacity\": 20,"
            + "\"stock\": 5"
            + "}";

    given().contentType(ContentType.JSON).body(requestBody1).when().post(WAREHOUSE_ENDPOINT);

    // Try to create second warehouse at same location
    String buCode2 = "HELMOND-2-" + System.currentTimeMillis();
    String requestBody2 =
        "{"
            + "\"businessUnitCode\": \""
            + buCode2
            + "\","
            + "\"location\": \"HELMOND-001\","
            + "\"capacity\": 15,"
            + "\"stock\": 3"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody2)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(400)
        .body(containsString("Maximum number of warehouses"));
  }

  @Test
  @Order(8)
  @DisplayName("Should archive warehouse successfully")
  void testArchiveWarehouse_Success() {
    // Create a warehouse to archive
    String buCode = "ARCHIVE-TEST-" + System.currentTimeMillis();
    String requestBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"VETSBY-001\","
            + "\"capacity\": 30,"
            + "\"stock\": 10"
            + "}";

    Response createResponse = given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200)
        .extract()
        .response();

    String warehouseId = createResponse.jsonPath().getString("id");

    // Archive it using the ID
    given().when().delete(WAREHOUSE_ENDPOINT + "/" + warehouseId).then().statusCode(204);
  }

  @Test
  @Order(9)
  @DisplayName("Should fail to archive non-existent warehouse")
  void testArchiveWarehouse_NotFound() {
    // Use a numeric ID that doesn't exist
    given()
        .when()
        .delete(WAREHOUSE_ENDPOINT + "/999999")
        .then()
        .statusCode(404)
        .body(containsString("not found"));
  }

  @Test
  @Order(10)
  @DisplayName("Should replace warehouse successfully")
  void testReplaceWarehouse_Success() {
    // Create initial warehouse
    String buCode = "REPLACE-TEST-" + System.currentTimeMillis();
    String createBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"EINDHOVEN-001\","
            + "\"capacity\": 20,"
            + "\"stock\": 10"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200);

    // Replace it with different location but same stock
    String replaceBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"ZWOLLE-002\","
            + "\"capacity\": 25,"
            + "\"stock\": 10"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(replaceBody)
        .when()
        .post(WAREHOUSE_ENDPOINT + "/" + buCode + "/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is(buCode))
        .body("location", is("ZWOLLE-002"))
        .body("capacity", is(25))
        .body("stock", is(10))
        .body("id", notNullValue());
  }

  @Test
  @Order(11)
  @DisplayName("Should fail to replace with mismatched stock")
  void testReplaceWarehouse_StockMismatch() {
    // Create initial warehouse
    String buCode = "REPLACE-STOCK-" + System.currentTimeMillis();
    String createBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"VETSBY-001\","
            + "\"capacity\": 30,"
            + "\"stock\": 20"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200);

    // Try to replace with different stock (25 instead of 20)
    String replaceBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"VETSBY-001\","
            + "\"capacity\": 40,"
            + "\"stock\": 25"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(replaceBody)
        .when()
        .post(WAREHOUSE_ENDPOINT + "/" + buCode + "/replacement")
        .then()
        .statusCode(400)
        .body(containsString("stock"));
  }

  @Test
  @Order(12)
  @DisplayName("Should fail to replace when new location has insufficient capacity")
  void testReplaceWarehouse_InsufficientCapacity() {
    // Create initial warehouse
    String buCode = "REPLACE-CAP-" + System.currentTimeMillis();
    String createBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"EINDHOVEN-001\","
            + "\"capacity\": 25,"
            + "\"stock\": 20"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200);

    // Try to replace with capacity less than stock (15 < 20)
    String replaceBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"ZWOLLE-002\","
            + "\"capacity\": 15,"
            + "\"stock\": 20"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(replaceBody)
        .when()
        .post(WAREHOUSE_ENDPOINT + "/" + buCode + "/replacement")
        .then()
        .statusCode(400)
        .body(containsString("cannot accommodate stock"));
  }

  @Test
  @Order(13)
  @DisplayName("Should fail to replace with invalid new location")
  void testReplaceWarehouse_InvalidLocation() {
    // Create initial warehouse at a location with available slots
    String buCode = "REPLACE-INVALIDLOC-" + System.currentTimeMillis();
    String createBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"AMSTERDAM-002\","
            + "\"capacity\": 25,"
            + "\"stock\": 15"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(createBody)
        .when()
        .post(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200);

    // Try to replace with invalid location
    String replaceBody =
        "{"
            + "\"businessUnitCode\": \""
            + buCode
            + "\","
            + "\"location\": \"INVALID-LOC-999\","
            + "\"capacity\": 30,"
            + "\"stock\": 15"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(replaceBody)
        .when()
        .post(WAREHOUSE_ENDPOINT + "/" + buCode + "/replacement")
        .then()
        .statusCode(404)
        .body(containsString("not found"));
  }

  @Test
  @Order(14)
  @DisplayName("Should list all warehouses")
  void testListAllWarehouses() {
    given()
        .when()
        .get(WAREHOUSE_ENDPOINT)
        .then()
        .statusCode(200)
        .body("$", not(empty()));
  }

  @Test
  @Order(15)
  @DisplayName("Should fail to get non-existent warehouse")
  void testGetWarehouse_NotFound() {
    // Use a numeric ID that doesn't exist
    given()
        .when()
        .get(WAREHOUSE_ENDPOINT + "/999999")
        .then()
        .statusCode(404)
        .body(containsString("not found"));
  }

  @Test
  @Order(16)
  @DisplayName("Should fail to replace non-existent warehouse")
  void testReplaceWarehouse_NotFound() {
    String replaceBody =
        "{"
            + "\"businessUnitCode\": \"NONEXISTENT-999\","
            + "\"location\": \"AMSTERDAM-001\","
            + "\"capacity\": 30,"
            + "\"stock\": 10"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(replaceBody)
        .when()
        .post(WAREHOUSE_ENDPOINT + "/NONEXISTENT-999/replacement")
        .then()
        .statusCode(404)
        .body(containsString("not found"));
  }
}
