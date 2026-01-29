package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@ApplicationScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Inject WarehouseStore warehouseStore;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> warehouses =
        warehouseStore.getAll();
    return warehouses.stream()
        .map(this::toApiWarehouse)
        .toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      // Convert API bean to domain model
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
          toDomainWarehouse(data);

      // Execute create use case with validations
      createWarehouseOperation.create(domainWarehouse);

      // Return the created warehouse as API bean
      return data;
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
        warehouseStore.findByBusinessUnitCode(id);

    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with id " + id + " not found", 404);
    }

    return toApiWarehouse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    try {
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
          new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
      warehouse.businessUnitCode = id;

      archiveWarehouseOperation.archive(warehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, Warehouse data) {
    try {
      // Convert API bean to domain model
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
          toDomainWarehouse(data);
      domainWarehouse.businessUnitCode = businessUnitCode;

      // Execute replace use case with validations
      replaceWarehouseOperation.replace(domainWarehouse);

      // Return the replaced warehouse as API bean
      return data;
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  // Convert API bean to domain model
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      toDomainWarehouse(Warehouse apiWarehouse) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = apiWarehouse.getId();
    domainWarehouse.location = apiWarehouse.getLocation();
    domainWarehouse.capacity = apiWarehouse.getCapacity();
    domainWarehouse.stock = apiWarehouse.getStock();
    return domainWarehouse;
  }

  // Convert domain model to API bean
  private Warehouse toApiWarehouse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse) {
    Warehouse apiWarehouse = new Warehouse();
    apiWarehouse.setId(domainWarehouse.businessUnitCode);
    apiWarehouse.setLocation(domainWarehouse.location);
    apiWarehouse.setCapacity(domainWarehouse.capacity);
    apiWarehouse.setStock(domainWarehouse.stock);
    return apiWarehouse;
  }
}
