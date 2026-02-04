package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exceptions.BusinessValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Replacing warehouse with business unit code: %s", newWarehouse.businessUnitCode);

    // Find the existing warehouse to be replaced
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      LOGGER.warnf(
          "Warehouse replacement failed: Business unit code %s not found",
          newWarehouse.businessUnitCode);
      throw new WarehouseNotFoundException(newWarehouse.businessUnitCode);
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new BusinessValidationException("Location " + newWarehouse.location + " is not valid");
    }

    // Stock Matching - new warehouse stock must match old warehouse stock
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new BusinessValidationException(
          "New warehouse stock ("
              + newWarehouse.stock
              + ") must match old warehouse stock ("
              + oldWarehouse.stock
              + ")");
    }

    // Capacity Accommodation - new warehouse capacity must accommodate the stock
    if (newWarehouse.capacity < newWarehouse.stock) {
      throw new BusinessValidationException(
          "New warehouse capacity ("
              + newWarehouse.capacity
              + ") cannot accommodate stock ("
              + newWarehouse.stock
              + ")");
    }

    // Capacity validation for location
    WarehouseRepository repository = (WarehouseRepository) warehouseStore;
    int currentTotalCapacity = repository.getTotalCapacityAtLocation(newWarehouse.location);

    // If staying at same location, subtract old capacity
    if (newWarehouse.location.equals(oldWarehouse.location)) {
      currentTotalCapacity -= oldWarehouse.capacity;
    }

    int newTotalCapacity = currentTotalCapacity + newWarehouse.capacity;
    if (newTotalCapacity > location.maxCapacity) {
      throw new BusinessValidationException(
          "Total capacity "
              + newTotalCapacity
              + " would exceed location's maximum capacity of "
              + location.maxCapacity);
    }

    // All validations passed, update the warehouse
    warehouseStore.update(newWarehouse);

    LOGGER.infof(
        "Successfully replaced warehouse with business unit code: %s",
        newWarehouse.businessUnitCode);
  }
}
