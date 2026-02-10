package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return listAll().stream()
        .map(this::toDomainWarehouse)
        .toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = toDbWarehouse(warehouse);
    dbWarehouse.createdAt = LocalDateTime.now();
    persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse =
        find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse == null) {
      throw new WarehouseNotFoundException(warehouse.businessUnitCode);
    }

    // Update fields
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    if (warehouse.archivedAt != null) {
      dbWarehouse.archivedAt = warehouse.archivedAt;
    }

    persist(dbWarehouse);
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse dbWarehouse =
        find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse != null) {
      delete(dbWarehouse);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? toDomainWarehouse(dbWarehouse) : null;
  }

  @Override
  public Warehouse findWarehouseById(Long id) {
    DbWarehouse dbWarehouse = find("id", id).firstResult();
    return dbWarehouse != null ? toDomainWarehouse(dbWarehouse) : null;
  }

  /**
   * Count non-archived warehouses at a specific location
   */
  public long countActiveWarehousesAtLocation(String locationId) {
    return count("location = ?1 and archivedAt is null", locationId);
  }

  /**
   * Get total capacity of all non-archived warehouses at a location
   */
  public int getTotalCapacityAtLocation(String locationId) {
    List<DbWarehouse> warehouses =
        list("location = ?1 and archivedAt is null", locationId);
    return warehouses.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();
  }

  // Convert domain model to database entity
  private DbWarehouse toDbWarehouse(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;

    if (warehouse.createdAt != null) {
      dbWarehouse.createdAt = warehouse.createdAt;
    }
    if (warehouse.archivedAt != null) {
      dbWarehouse.archivedAt = warehouse.archivedAt;
    }

    return dbWarehouse;
  }

  // Convert database entity to domain model
  private Warehouse toDomainWarehouse(DbWarehouse dbWarehouse) {
    Warehouse warehouse = new Warehouse();
    warehouse.id = dbWarehouse.id;
    warehouse.businessUnitCode = dbWarehouse.businessUnitCode;
    warehouse.location = dbWarehouse.location;
    warehouse.capacity = dbWarehouse.capacity;
    warehouse.stock = dbWarehouse.stock;

    if (dbWarehouse.createdAt != null) {
      warehouse.createdAt = dbWarehouse.createdAt;
    }
    if (dbWarehouse.archivedAt != null) {
      warehouse.archivedAt = dbWarehouse.archivedAt;
    }

    return warehouse;
  }
}