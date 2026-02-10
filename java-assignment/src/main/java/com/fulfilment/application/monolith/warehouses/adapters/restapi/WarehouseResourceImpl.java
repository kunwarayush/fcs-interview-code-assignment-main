package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
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
    // Convert API bean to domain model
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        toDomainWarehouse(data);

    // Execute create use case with validations
    // Exceptions (LocationNotFoundException, IllegalArgumentException) are handled by ExceptionMappers
    createWarehouseOperation.create(domainWarehouse);

    // Fetch the created warehouse to get the assigned ID
    Warehouse createdWarehouse =
        warehouseStore.findByBusinessUnitCode(data.getBusinessUnitCode());

    // Return the created warehouse with ID populated
    return toApiWarehouse(createdWarehouse);
  }

  @Override
  @Transactional
  public Warehouse getAWarehouseUnitByID(String id) {
    Long warehouseId = Long.parseLong(id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
        warehouseStore.findWarehouseById(warehouseId);

    if (warehouse == null) {
      throw new WarehouseNotFoundException(warehouseId);
    }

    return toApiWarehouse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    Long warehouseId = Long.parseLong(id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
        warehouseStore.findWarehouseById(warehouseId);

    if (warehouse == null) {
      throw new WarehouseNotFoundException(warehouseId);
    }

    // WarehouseNotFoundException is handled by ExceptionMapper
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, Warehouse data) {
    // Convert API bean to domain model
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        toDomainWarehouse(data);
    domainWarehouse.businessUnitCode = businessUnitCode;

    // Execute replace use case with validations
    // Exceptions (WarehouseNotFoundException, LocationNotFoundException, IllegalArgumentException)
    // are handled by ExceptionMappers
    replaceWarehouseOperation.replace(domainWarehouse);

    // Fetch the replaced warehouse to get the new ID
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse replacedWarehouse =
        warehouseStore.findByBusinessUnitCode(businessUnitCode);

    // Return the replaced warehouse with ID populated
    return toApiWarehouse(replacedWarehouse);
  }

  // Convert API bean to domain model
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      toDomainWarehouse(Warehouse apiWarehouse) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    domainWarehouse.location = apiWarehouse.getLocation();
    domainWarehouse.capacity = apiWarehouse.getCapacity();
    domainWarehouse.stock = apiWarehouse.getStock();
    return domainWarehouse;
  }

  // Convert domain model to API bean
  private Warehouse toApiWarehouse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse) {
    Warehouse apiWarehouse = new Warehouse();
    if (domainWarehouse.id != null) {
      apiWarehouse.setId(String.valueOf(domainWarehouse.id));
    }
    apiWarehouse.setBusinessUnitCode(domainWarehouse.businessUnitCode);
    apiWarehouse.setLocation(domainWarehouse.location);
    apiWarehouse.setCapacity(domainWarehouse.capacity);
    apiWarehouse.setStock(domainWarehouse.stock);
    return apiWarehouse;
  }
}
