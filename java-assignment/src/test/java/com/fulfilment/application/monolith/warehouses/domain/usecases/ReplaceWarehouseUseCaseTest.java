package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.exceptions.BusinessValidationException;
import com.fulfilment.application.monolith.location.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ReplaceWarehouseUseCase
 *
 * <p>Tests cover: - Positive scenario: successful warehouse replacement - Negative scenarios:
 * warehouse not found, invalid location, stock mismatch, insufficient capacity, location capacity
 * exceeded
 */
class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private LocationResolver locationResolver;

  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseRepository, locationResolver);
  }

  @Test
  @DisplayName("Should successfully replace warehouse when all validations pass")
  void testReplaceWarehouse_Success() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();
    Location location = new Location("AMSTERDAM-002", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-002")).thenReturn(40);

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    verify(warehouseRepository).update(newWarehouse);
  }

  @Test
  @DisplayName("Should throw exception when warehouse does not exist")
  void testReplaceWarehouse_WarehouseNotFound() {
    // Given
    Warehouse newWarehouse = createNewWarehouse();

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(null);

    // When & Then
    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "Warehouse with business unit code MWH.100 not found", exception.getMessage());
    assertEquals("MWH.100", exception.getIdentifier());
    verify(warehouseRepository, never()).update(any());
  }

  @Test
  @DisplayName("Should throw exception when new location is invalid")
  void testReplaceWarehouse_InvalidLocation() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002"))
        .thenThrow(new LocationNotFoundException("AMSTERDAM-002"));

    // When & Then
    LocationNotFoundException exception =
        assertThrows(
            LocationNotFoundException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertTrue(exception.getMessage().contains("Location with identifier AMSTERDAM-002 not found"));
    assertEquals("AMSTERDAM-002", exception.getIdentifier());
    verify(warehouseRepository, never()).update(any());
  }

  @Test
  @DisplayName("Should throw exception when stock does not match")
  void testReplaceWarehouse_StockMismatch() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();
    newWarehouse.stock = 20; // Different from old stock
    Location location = new Location("AMSTERDAM-002", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(location);

    // When & Then
    BusinessValidationException exception =
        assertThrows(
            BusinessValidationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "New warehouse stock (20) must match old warehouse stock (10)", exception.getMessage());
    verify(warehouseRepository, never()).update(any());
  }

  @Test
  @DisplayName("Should throw exception when new capacity cannot accommodate stock")
  void testReplaceWarehouse_InsufficientCapacity() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();
    newWarehouse.capacity = 5; // Less than stock (10)
    Location location = new Location("AMSTERDAM-002", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(location);

    // When & Then
    BusinessValidationException exception =
        assertThrows(
            BusinessValidationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "New warehouse capacity (5) cannot accommodate stock (10)", exception.getMessage());
    verify(warehouseRepository, never()).update(any());
  }

  @Test
  @DisplayName("Should throw exception when new capacity exceeds location max capacity")
  void testReplaceWarehouse_ExceedsLocationMaxCapacity() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();
    newWarehouse.capacity = 70;
    Location location = new Location("AMSTERDAM-002", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-002")).thenReturn(50);

    // When & Then
    BusinessValidationException exception =
        assertThrows(
            BusinessValidationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "Total capacity 120 would exceed location's maximum capacity of 100",
        exception.getMessage());
    verify(warehouseRepository, never()).update(any());
  }

  @Test
  @DisplayName("Should successfully replace warehouse at same location")
  void testReplaceWarehouse_SameLocation() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    oldWarehouse.location = "AMSTERDAM-001";
    Warehouse newWarehouse = createNewWarehouse();
    newWarehouse.location = "AMSTERDAM-001"; // Same location
    newWarehouse.capacity = 40;
    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-001")).thenReturn(60);

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then (60 - 30 + 40 = 70, which is less than 100)
    verify(warehouseRepository).update(newWarehouse);
  }

  @Test
  @DisplayName("Should successfully replace warehouse when exactly at capacity limit")
  void testReplaceWarehouse_ExactlyAtCapacityLimit() {
    // Given
    Warehouse oldWarehouse = createOldWarehouse();
    Warehouse newWarehouse = createNewWarehouse();
    newWarehouse.capacity = 60;
    Location location = new Location("AMSTERDAM-002", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(location);
    when(warehouseRepository.getTotalCapacityAtLocation("AMSTERDAM-002")).thenReturn(40);

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then (40 + 60 = 100, which equals max capacity)
    verify(warehouseRepository).update(newWarehouse);
  }

  // Helper methods
  private Warehouse createOldWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 30;
    warehouse.stock = 10;
    return warehouse;
  }

  private Warehouse createNewWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";
    warehouse.location = "AMSTERDAM-002";
    warehouse.capacity = 40;
    warehouse.stock = 10; // Must match old warehouse stock
    return warehouse;
  }
}
