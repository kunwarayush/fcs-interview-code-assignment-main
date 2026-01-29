package com.fulfilment.application.monolith.fulfillment;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * REST API endpoint for managing fulfillment associations between Products, Warehouses, and
 * Stores.
 *
 * <p>This resource provides endpoints for: - Creating new fulfillment associations - Retrieving
 * fulfillments by Store, Product, or Warehouse - Deleting fulfillment associations - Retrieving
 * statistics about fulfillments
 */
@Path("/api/fulfillment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

  private static final Logger LOG = Logger.getLogger(FulfillmentResource.class);

  @Inject ProductWarehouseFulfillmentRepository fulfillmentRepository;

  @Inject FulfillmentValidationService validationService;

  /**
   * Create a new fulfillment association.
   *
   * <p>POST /api/fulfillment
   *
   * <p>Request body example:
   *
   * <pre>
   * {
   *   "productId": 1,
   *   "warehouseBusinessUnit": "WH-001",
   *   "storeId": 1
   * }
   * </pre>
   *
   * @param request the fulfillment request
   * @return 201 Created with the created fulfillment
   */
  @POST
  @Transactional
  public Response createFulfillment(@Valid FulfillmentRequest request) {
    LOG.infof(
        "Creating fulfillment: Product %d, Warehouse %s, Store %d",
        request.productId, request.warehouseBusinessUnit, request.storeId);

    // Validate the request
    validationService.validateFulfillmentCreation(
        request.productId, request.warehouseBusinessUnit, request.storeId);

    // Create the fulfillment
    ProductWarehouseFulfillment fulfillment =
        new ProductWarehouseFulfillment(
            request.productId, request.warehouseBusinessUnit, request.storeId);

    fulfillmentRepository.persist(fulfillment);

    LOG.infof(
        "Fulfillment created successfully: Product %d, Warehouse %s, Store %d",
        request.productId, request.warehouseBusinessUnit, request.storeId);

    return Response.created(
            URI.create(
                "/api/fulfillment/product/"
                    + request.productId
                    + "/warehouse/"
                    + request.warehouseBusinessUnit
                    + "/store/"
                    + request.storeId))
        .entity(FulfillmentResponse.from(fulfillment))
        .build();
  }

  /**
   * Get all fulfillment associations for a specific store.
   *
   * <p>GET /api/fulfillment/store/{storeId}
   *
   * @param storeId the store ID
   * @return list of fulfillment associations
   */
  @GET
  @Path("/store/{storeId}")
  public Response getFulfillmentsByStore(@PathParam("storeId") Long storeId) {
    LOG.infof("Retrieving fulfillments for Store %d", storeId);

    List<ProductWarehouseFulfillment> fulfillments =
        fulfillmentRepository.findByStoreId(storeId);

    List<FulfillmentResponse> responses =
        fulfillments.stream().map(FulfillmentResponse::from).collect(Collectors.toList());

    return Response.ok(responses).build();
  }

  /**
   * Get all fulfillment associations for a specific product.
   *
   * <p>GET /api/fulfillment/product/{productId}
   *
   * @param productId the product ID
   * @return list of fulfillment associations
   */
  @GET
  @Path("/product/{productId}")
  public Response getFulfillmentsByProduct(@PathParam("productId") Long productId) {
    LOG.infof("Retrieving fulfillments for Product %d", productId);

    List<ProductWarehouseFulfillment> fulfillments =
        fulfillmentRepository.findByProductId(productId);

    List<FulfillmentResponse> responses =
        fulfillments.stream().map(FulfillmentResponse::from).collect(Collectors.toList());

    return Response.ok(responses).build();
  }

  /**
   * Get all fulfillment associations for a specific warehouse.
   *
   * <p>GET /api/fulfillment/warehouse/{warehouseBusinessUnit}
   *
   * @param warehouseBusinessUnit the warehouse business unit code
   * @return list of fulfillment associations
   */
  @GET
  @Path("/warehouse/{warehouseBusinessUnit}")
  public Response getFulfillmentsByWarehouse(
      @PathParam("warehouseBusinessUnit") String warehouseBusinessUnit) {
    LOG.infof("Retrieving fulfillments for Warehouse %s", warehouseBusinessUnit);

    List<ProductWarehouseFulfillment> fulfillments =
        fulfillmentRepository.findByWarehouseBusinessUnit(warehouseBusinessUnit);

    List<FulfillmentResponse> responses =
        fulfillments.stream().map(FulfillmentResponse::from).collect(Collectors.toList());

    return Response.ok(responses).build();
  }

  /**
   * Get all fulfillment associations.
   *
   * <p>GET /api/fulfillment
   *
   * @return list of all fulfillment associations
   */
  @GET
  public Response getAllFulfillments() {
    LOG.info("Retrieving all fulfillments");

    List<ProductWarehouseFulfillment> fulfillments = fulfillmentRepository.listAll();

    List<FulfillmentResponse> responses =
        fulfillments.stream().map(FulfillmentResponse::from).collect(Collectors.toList());

    return Response.ok(responses).build();
  }

  /**
   * Delete a fulfillment association.
   *
   * <p>DELETE /api/fulfillment
   *
   * <p>Request body example:
   *
   * <pre>
   * {
   *   "productId": 1,
   *   "warehouseBusinessUnit": "WH-001",
   *   "storeId": 1
   * }
   * </pre>
   *
   * @param request the fulfillment request
   * @return 204 No Content
   */
  @DELETE
  @Transactional
  public Response deleteFulfillment(@Valid FulfillmentRequest request) {
    LOG.infof(
        "Deleting fulfillment: Product %d, Warehouse %s, Store %d",
        request.productId, request.warehouseBusinessUnit, request.storeId);

    // Validate the request
    validationService.validateFulfillmentDeletion(
        request.productId, request.warehouseBusinessUnit, request.storeId);

    // Delete the fulfillment
    fulfillmentRepository.deleteById(
        new ProductWarehouseFulfillmentId(
            request.productId, request.warehouseBusinessUnit, request.storeId));

    LOG.infof(
        "Fulfillment deleted successfully: Product %d, Warehouse %s, Store %d",
        request.productId, request.warehouseBusinessUnit, request.storeId);

    return Response.noContent().build();
  }

  /**
   * Get statistics for a specific store.
   *
   * <p>GET /api/fulfillment/store/{storeId}/stats
   *
   * @param storeId the store ID
   * @return statistics about the store's fulfillments
   */
  @GET
  @Path("/store/{storeId}/stats")
  public Response getStoreStats(@PathParam("storeId") Long storeId) {
    LOG.infof("Retrieving statistics for Store %d", storeId);

    long distinctWarehouseCount = fulfillmentRepository.countDistinctWarehousesForStore(storeId);
    long totalFulfillments = fulfillmentRepository.findByStoreId(storeId).size();

    FulfillmentStats stats =
        new FulfillmentStats(
            "Store",
            storeId.toString(),
            distinctWarehouseCount,
            3L,
            totalFulfillments,
            distinctWarehouseCount < 3);

    return Response.ok(stats).build();
  }

  /**
   * Get statistics for a specific product.
   *
   * <p>GET /api/fulfillment/product/{productId}/stats
   *
   * @param productId the product ID
   * @return statistics about the product's fulfillments
   */
  @GET
  @Path("/product/{productId}/stats")
  public Response getProductStats(@PathParam("productId") Long productId) {
    LOG.infof("Retrieving statistics for Product %d", productId);

    List<ProductWarehouseFulfillment> fulfillments =
        fulfillmentRepository.findByProductId(productId);

    long totalFulfillments = fulfillments.size();
    long storesCount = fulfillments.stream().map(f -> f.getStoreId()).distinct().count();
    long warehousesCount =
        fulfillments.stream().map(f -> f.getWarehouseBusinessUnit()).distinct().count();

    FulfillmentStats stats =
        new FulfillmentStats(
            "Product",
            productId.toString(),
            warehousesCount,
            null,
            totalFulfillments,
            true,
            storesCount);

    return Response.ok(stats).build();
  }

  /**
   * Get statistics for a specific warehouse.
   *
   * <p>GET /api/fulfillment/warehouse/{warehouseBusinessUnit}/stats
   *
   * @param warehouseBusinessUnit the warehouse business unit code
   * @return statistics about the warehouse's fulfillments
   */
  @GET
  @Path("/warehouse/{warehouseBusinessUnit}/stats")
  public Response getWarehouseStats(
      @PathParam("warehouseBusinessUnit") String warehouseBusinessUnit) {
    LOG.infof("Retrieving statistics for Warehouse %s", warehouseBusinessUnit);

    long distinctProductCount =
        fulfillmentRepository.countDistinctProductsInWarehouse(warehouseBusinessUnit);
    long totalFulfillments =
        fulfillmentRepository.findByWarehouseBusinessUnit(warehouseBusinessUnit).size();

    FulfillmentStats stats =
        new FulfillmentStats(
            "Warehouse",
            warehouseBusinessUnit,
            distinctProductCount,
            5L,
            totalFulfillments,
            distinctProductCount < 5);

    return Response.ok(stats).build();
  }

  /** Statistics DTO */
  public static class FulfillmentStats {
    public String entityType;
    public String entityId;
    public Long currentCount;
    public Long maxAllowed;
    public Long totalFulfillments;
    public Boolean canAddMore;
    public Long additionalCount;

    public FulfillmentStats(
        String entityType,
        String entityId,
        Long currentCount,
        Long maxAllowed,
        Long totalFulfillments,
        Boolean canAddMore) {
      this.entityType = entityType;
      this.entityId = entityId;
      this.currentCount = currentCount;
      this.maxAllowed = maxAllowed;
      this.totalFulfillments = totalFulfillments;
      this.canAddMore = canAddMore;
    }

    public FulfillmentStats(
        String entityType,
        String entityId,
        Long currentCount,
        Long maxAllowed,
        Long totalFulfillments,
        Boolean canAddMore,
        Long additionalCount) {
      this(entityType, entityId, currentCount, maxAllowed, totalFulfillments, canAddMore);
      this.additionalCount = additionalCount;
    }
  }
}
