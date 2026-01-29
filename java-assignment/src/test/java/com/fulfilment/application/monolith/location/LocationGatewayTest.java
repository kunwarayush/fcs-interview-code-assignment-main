package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LocationGateway
 *
 * <p>Tests cover: - Positive scenario: finding valid locations - Negative scenario: location not
 * found - Boundary conditions: all pre-configured locations
 */
public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  @DisplayName("Should successfully resolve existing location")
  public void testWhenResolveExistingLocationShouldReturn() {
    // When
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // Then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  @DisplayName("Should throw exception for non-existent location")
  void testResolveByIdentifier_NotFound() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> locationGateway.resolveByIdentifier("INVALID-001"));

    assertEquals("Location with identifier INVALID-001 not found.", exception.getMessage());
  }

  @Test
  @DisplayName("Should resolve all pre-configured locations")
  void testResolveByIdentifier_AllLocations() {
    // Test all configured locations
    assertLocationExists("ZWOLLE-001", 1, 40);
    assertLocationExists("ZWOLLE-002", 2, 50);
    assertLocationExists("AMSTERDAM-001", 5, 100);
    assertLocationExists("AMSTERDAM-002", 3, 75);
    assertLocationExists("TILBURG-001", 1, 40);
    assertLocationExists("HELMOND-001", 1, 45);
    assertLocationExists("EINDHOVEN-001", 2, 70);
    assertLocationExists("VETSBY-001", 1, 90);
  }

  @Test
  @DisplayName("Should throw exception for null identifier")
  void testResolveByIdentifier_NullIdentifier() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(null));
  }

  @Test
  @DisplayName("Should throw exception for empty identifier")
  void testResolveByIdentifier_EmptyIdentifier() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> locationGateway.resolveByIdentifier(""));

    assertEquals("Location with identifier  not found.", exception.getMessage());
  }

  private void assertLocationExists(
      String identifier, int expectedMaxWarehouses, int expectedMaxCapacity) {
    Location location = locationGateway.resolveByIdentifier(identifier);
    assertNotNull(location);
    assertEquals(identifier, location.identification);
    assertEquals(expectedMaxWarehouses, location.maxNumberOfWarehouses);
    assertEquals(expectedMaxCapacity, location.maxCapacity);
  }
}
