package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exceptions.BusinessValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof(
        "Creating warehouse with business unit code: %s at location: %s",
        warehouse.businessUnitCode, warehouse.location);

    // 1. Business Unit Code Verification - must be unique
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      LOGGER.warnf(
          "Warehouse creation failed: Business unit code %s already exists",
          warehouse.businessUnitCode);
      throw new BusinessValidationException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists");
    }

    // 2. Location Validation - must be valid
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new BusinessValidationException("Location " + warehouse.location + " is not valid");
    }

    // 3. Warehouse Creation Feasibility - check max number of warehouses
    WarehouseRepository repository = (WarehouseRepository) warehouseStore;
    long warehouseCount = repository.countActiveWarehousesAtLocation(warehouse.location);
    if (warehouseCount >= location.maxNumberOfWarehouses) {
      throw new BusinessValidationException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") reached for location "
              + warehouse.location);
    }

    // 4. Capacity and Stock Validation
    // Validate capacity doesn't exceed location's max capacity
    int currentTotalCapacity = repository.getTotalCapacityAtLocation(warehouse.location);
    int newTotalCapacity = currentTotalCapacity + warehouse.capacity;
    if (newTotalCapacity > location.maxCapacity) {
      throw new BusinessValidationException(
          "Total capacity "
              + newTotalCapacity
              + " would exceed location's maximum capacity of "
              + location.maxCapacity);
    }

    // Validate warehouse capacity can handle its stock
    if (warehouse.stock > warehouse.capacity) {
      throw new BusinessValidationException(
          "Warehouse stock ("
              + warehouse.stock
              + ") exceeds capacity ("
              + warehouse.capacity
              + ")");
    }

    // All validations passed, create the warehouse
    warehouseStore.create(warehouse);
    LOGGER.infof(
        "Successfully created warehouse with business unit code: %s", warehouse.businessUnitCode);
  }
}
