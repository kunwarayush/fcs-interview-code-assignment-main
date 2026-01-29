package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for CreateWarehouseUseCase
 *
 * <p>Tests cover: - Positive scenario: successful warehouse creation - Negative scenarios: business
 * unit code duplication, invalid location, exceeding max warehouses, capacity constraints, stock
 * validation
 */
class CreateWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private LocationResolver locationResolver;

  private CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseRepository, locationResolver);
  }

  @Test
  @DisplayName("Should successfully create warehouse when all validations pass")
  void testCreateWarehouse_Success() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    verify(warehouseRepository).create(warehouse);
  }

  @Test
  @DisplayName("Should throw exception when business unit code already exists")
  void testCreateWarehouse_DuplicateBusinessUnitCode() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.100";

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(existingWarehouse);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Warehouse with business unit code MWH.100 already exists", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  @DisplayName("Should throw exception when location is invalid")
  void testCreateWarehouse_InvalidLocation() {
    // Given
    Warehouse warehouse = createValidWarehouse();

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenThrow(new IllegalArgumentException("Location with identifier AMSTERDAM-001 not found."));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));

    assertTrue(exception.getMessage().contains("Location with identifier AMSTERDAM-001 not found"));
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  @DisplayName("Should throw exception when max number of warehouses reached")
  void testCreateWarehouse_MaxWarehousesReached() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    Location location = new Location("AMSTERDAM-001", 3, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(3L);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Maximum number of warehouses (3) reached for location AMSTERDAM-001",
        exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  @DisplayName("Should throw exception when total capacity exceeds location max capacity")
  void testCreateWarehouse_ExceedsLocationMaxCapacity() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    warehouse.capacity = 60;
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(50);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Total capacity 110 would exceed location's maximum capacity of 100",
        exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  @DisplayName("Should throw exception when stock exceeds capacity")
  void testCreateWarehouse_StockExceedsCapacity() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    warehouse.capacity = 20;
    warehouse.stock = 30;
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(40);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals("Warehouse stock (30) exceeds capacity (20)", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  @DisplayName("Should successfully create warehouse when exactly at location capacity limit")
  void testCreateWarehouse_ExactlyAtCapacityLimit() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    warehouse.capacity = 60;
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    verify(warehouseRepository).create(warehouse);
  }

  @Test
  @DisplayName("Should successfully create warehouse when exactly at max warehouse limit")
  void testCreateWarehouse_ExactlyAtWarehouseLimit() {
    // Given
    Warehouse warehouse = createValidWarehouse();
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesAtLocation("AMSTERDAM-001")).thenReturn(4L);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    verify(warehouseRepository).create(warehouse);
  }

  // Helper method to create a valid warehouse for testing
  private Warehouse createValidWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;
    return warehouse;
  }
}
