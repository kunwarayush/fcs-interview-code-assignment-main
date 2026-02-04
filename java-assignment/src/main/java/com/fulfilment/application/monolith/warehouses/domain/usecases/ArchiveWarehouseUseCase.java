package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    LOGGER.infof("Archiving warehouse with business unit code: %s", warehouse.businessUnitCode);

    // Find the existing warehouse
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      LOGGER.warnf(
          "Warehouse archival failed: Business unit code %s not found", warehouse.businessUnitCode);
      throw new WarehouseNotFoundException(warehouse.businessUnitCode);
    }

    // Set the archived timestamp
    existing.archivedAt = LocalDateTime.now();

    // Update the warehouse with the archived timestamp
    warehouseStore.update(existing);
    LOGGER.infof(
        "Successfully archived warehouse with business unit code: %s", warehouse.businessUnitCode);
  }
}
