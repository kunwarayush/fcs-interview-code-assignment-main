package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for ArchiveWarehouseUseCase
 *
 * <p>Tests cover: - Positive scenario: successful warehouse archival - Negative scenario: warehouse
 * not found
 */
class ArchiveWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  @DisplayName("Should successfully archive warehouse")
  void testArchiveWarehouse_Success() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.100";
    existingWarehouse.location = "AMSTERDAM-001";
    existingWarehouse.capacity = 30;
    existingWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(existingWarehouse);

    // When
    archiveWarehouseUseCase.archive(warehouse);

    // Then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());

    Warehouse updatedWarehouse = captor.getValue();
    assertNotNull(updatedWarehouse.archivedAt);
    assertEquals("MWH.100", updatedWarehouse.businessUnitCode);
  }

  @Test
  @DisplayName("Should throw exception when warehouse does not exist")
  void testArchiveWarehouse_WarehouseNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.999";

    when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

    // When & Then
    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class, () -> archiveWarehouseUseCase.archive(warehouse));

    assertEquals(
        "Warehouse with business unit code MWH.999 not found", exception.getMessage());
    assertEquals("MWH.999", exception.getIdentifier());
    verify(warehouseStore, never()).update(any());
  }

  @Test
  @DisplayName("Should preserve warehouse data when archiving")
  void testArchiveWarehouse_PreservesData() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.100";

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.100";
    existingWarehouse.location = "AMSTERDAM-001";
    existingWarehouse.capacity = 50;
    existingWarehouse.stock = 25;

    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(existingWarehouse);

    // When
    archiveWarehouseUseCase.archive(warehouse);

    // Then
    ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(captor.capture());

    Warehouse updatedWarehouse = captor.getValue();
    assertEquals("AMSTERDAM-001", updatedWarehouse.location);
    assertEquals(50, updatedWarehouse.capacity);
    assertEquals(25, updatedWarehouse.stock);
    assertNotNull(updatedWarehouse.archivedAt);
  }
}
