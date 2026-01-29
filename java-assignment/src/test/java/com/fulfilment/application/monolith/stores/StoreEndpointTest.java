package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

/**
 * Integration tests for Store REST API endpoints
 * Tests CRUD operations with TransactionSynchronizationRegistry integration
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StoreEndpointTest {

  private static final String STORES_ENDPOINT = "/stores";
  private static long createdStoreId;

  @Test
  @Order(1)
  @DisplayName("Should create a new store")
  void testCreateStore_Success() {
    String requestBody =
        "{" + "\"name\": \"Test Store " + System.currentTimeMillis() + "\"," 
        + "\"quantityProductsInStock\": 100" + "}";

    String response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(STORES_ENDPOINT)
            .then()
            .statusCode(201)
            .body("name", containsString("Test Store"))
            .body("quantityProductsInStock", is(100))
            .extract()
            .asString();

    // Extract the ID for later tests
    createdStoreId = io.restassured.path.json.JsonPath.from(response).getLong("id");
  }

  @Test
  @Order(2)
  @DisplayName("Should get all stores")
  void testGetAllStores() {
    given()
        .when()
        .get(STORES_ENDPOINT)
        .then()
        .statusCode(200)
        .body("$", not(empty()));
  }

  @Test
  @Order(3)
  @DisplayName("Should get single store by ID")
  void testGetSingleStore_Success() {
    given()
        .when()
        .get(STORES_ENDPOINT + "/" + createdStoreId)
        .then()
        .statusCode(200)
        .body("id", is((int) createdStoreId))
        .body("name", containsString("Test Store"));
  }

  @Test
  @Order(4)
  @DisplayName("Should update store with PUT")
  void testUpdateStore_Success() {
    String requestBody =
        "{" + "\"name\": \"Updated Store " + System.currentTimeMillis() + "\"," 
        + "\"quantityProductsInStock\": 150" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .put(STORES_ENDPOINT + "/" + createdStoreId)
        .then()
        .statusCode(200)
        .body("name", containsString("Updated Store"))
        .body("quantityProductsInStock", is(150));
  }

  @Test
  @Order(5)
  @DisplayName("Should partially update store with PATCH")
  void testPatchStore_Success() {
    String requestBody =
        "{" + "\"name\": \"Patched Store " + System.currentTimeMillis() + "\"," 
        + "\"quantityProductsInStock\": 200" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .patch(STORES_ENDPOINT + "/" + createdStoreId)
        .then()
        .statusCode(200)
        .body("name", containsString("Patched Store"))
        .body("quantityProductsInStock", is(200));
  }

  @Test
  @Order(6)
  @DisplayName("Should fail to create store with ID set")
  void testCreateStore_WithIdSet() {
    String requestBody =
        "{"
            + "\"id\": 999,"
            + "\"name\": \"Invalid Store\","
            + "\"quantityProductsInStock\": 50"
            + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(STORES_ENDPOINT)
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set"));
  }

  @Test
  @Order(7)
  @DisplayName("Should fail to update non-existent store")
  void testUpdateStore_NotFound() {
    String requestBody =
        "{" + "\"name\": \"Non-existent Store\"," + "\"quantityProductsInStock\": 100" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .put(STORES_ENDPOINT + "/999999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  @Order(8)
  @DisplayName("Should fail to update store without name")
  void testUpdateStore_MissingName() {
    String requestBody = "{" + "\"quantityProductsInStock\": 100" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .put(STORES_ENDPOINT + "/" + createdStoreId)
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set"));
  }

  @Test
  @Order(9)
  @DisplayName("Should fail to patch non-existent store")
  void testPatchStore_NotFound() {
    String requestBody =
        "{" + "\"name\": \"Non-existent Store\"," + "\"quantityProductsInStock\": 100" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .patch(STORES_ENDPOINT + "/999999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  @Order(10)
  @DisplayName("Should fail to patch store without name")
  void testPatchStore_MissingName() {
    String requestBody = "{" + "\"quantityProductsInStock\": 100" + "}";

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .patch(STORES_ENDPOINT + "/" + createdStoreId)
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set"));
  }

  @Test
  @Order(11)
  @DisplayName("Should fail to get non-existent store")
  void testGetSingleStore_NotFound() {
    given().when().get(STORES_ENDPOINT + "/999999").then().statusCode(404);
  }

  @Test
  @Order(12)
  @DisplayName("Should delete store")
  void testDeleteStore_Success() {
    // Create a new store to delete
    String requestBody =
        "{"
            + "\"name\": \"Store to Delete "
            + System.currentTimeMillis()
            + "\","
            + "\"quantityProductsInStock\": 50"
            + "}";

    String response =
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post(STORES_ENDPOINT)
            .then()
            .statusCode(201)
            .extract()
            .asString();

    long storeIdToDelete = io.restassured.path.json.JsonPath.from(response).getLong("id");

    // Delete it
    given().when().delete(STORES_ENDPOINT + "/" + storeIdToDelete).then().statusCode(204);
  }

  @Test
  @Order(13)
  @DisplayName("Should fail to delete non-existent store")
  void testDeleteStore_NotFound() {
    given().when().delete(STORES_ENDPOINT + "/999999").then().statusCode(404);
  }
}
