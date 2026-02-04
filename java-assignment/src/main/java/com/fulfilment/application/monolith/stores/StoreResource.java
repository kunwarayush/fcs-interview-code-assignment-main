package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.exceptions.BusinessValidationException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

@Path("stores")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject TransactionSyncService transactionSyncService;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new StoreNotFoundException(id);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new BusinessValidationException("Id was invalidly set on request.");
    }

    store.persist();

    // Call legacy system only after successful database commit
    transactionSyncService.executeAfterCommit(
        () -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new BusinessValidationException("Store Name was not set on request.");
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new StoreNotFoundException(id);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    transactionSyncService.executeAfterCommit(
        () -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entity));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new BusinessValidationException("Store Name was not set on request.");
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new StoreNotFoundException(id);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    transactionSyncService.executeAfterCommit(
        () -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entity));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new StoreNotFoundException(id);
    }
    entity.delete();
    return Response.status(204).build();
  }
}
