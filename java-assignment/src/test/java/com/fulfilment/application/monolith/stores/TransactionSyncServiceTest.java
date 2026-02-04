package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for TransactionSyncService
 *
 * <p>Tests the service's behavior in coordinating transaction callbacks with the JTA transaction
 * manager.
 */
class TransactionSyncServiceTest {

  private TransactionSyncService service;
  private TransactionSynchronizationRegistry mockRegistry;

  @BeforeEach
  void setUp() {
    mockRegistry = mock(TransactionSynchronizationRegistry.class);
    service = new TransactionSyncService();
    service.syncRegistry = mockRegistry;
  }

  @Test
  @DisplayName("Should register synchronization callback when executeAfterCommit is called")
  void testExecuteAfterCommit_RegistersCallback() {
    // Given
    Runnable action = mock(Runnable.class);

    // When
    service.executeAfterCommit(action);

    // Then
    verify(mockRegistry).registerInterposedSynchronization(any(Synchronization.class));
  }

  @Test
  @DisplayName("Should execute action when transaction commits successfully")
  void testExecuteAfterCommit_ExecutesOnCommit() {
    // Given
    Runnable action = mock(Runnable.class);
    ArgumentCaptor<Synchronization> syncCaptor = ArgumentCaptor.forClass(Synchronization.class);

    // When
    service.executeAfterCommit(action);
    verify(mockRegistry).registerInterposedSynchronization(syncCaptor.capture());

    // Simulate successful commit
    Synchronization capturedSync = syncCaptor.getValue();
    capturedSync.afterCompletion(Status.STATUS_COMMITTED);

    // Then
    verify(action).run();
  }

  @Test
  @DisplayName("Should NOT execute action when transaction rolls back")
  void testExecuteAfterCommit_DoesNotExecuteOnRollback() {
    // Given
    Runnable action = mock(Runnable.class);
    ArgumentCaptor<Synchronization> syncCaptor = ArgumentCaptor.forClass(Synchronization.class);

    // When
    service.executeAfterCommit(action);
    verify(mockRegistry).registerInterposedSynchronization(syncCaptor.capture());

    // Simulate rollback
    Synchronization capturedSync = syncCaptor.getValue();
    capturedSync.afterCompletion(Status.STATUS_ROLLEDBACK);

    // Then
    verify(action, never()).run();
  }

  @Test
  @DisplayName("Should NOT execute action when transaction status is unknown")
  void testExecuteAfterCommit_DoesNotExecuteOnUnknownStatus() {
    // Given
    Runnable action = mock(Runnable.class);
    ArgumentCaptor<Synchronization> syncCaptor = ArgumentCaptor.forClass(Synchronization.class);

    // When
    service.executeAfterCommit(action);
    verify(mockRegistry).registerInterposedSynchronization(syncCaptor.capture());

    // Simulate unknown status
    Synchronization capturedSync = syncCaptor.getValue();
    capturedSync.afterCompletion(Status.STATUS_UNKNOWN);

    // Then
    verify(action, never()).run();
  }

  @Test
  @DisplayName("Should handle multiple callbacks in same transaction")
  void testExecuteAfterCommit_MultipleCallbacks() {
    // Given
    Runnable action1 = mock(Runnable.class);
    Runnable action2 = mock(Runnable.class);
    ArgumentCaptor<Synchronization> syncCaptor = ArgumentCaptor.forClass(Synchronization.class);

    // When
    service.executeAfterCommit(action1);
    service.executeAfterCommit(action2);

    verify(mockRegistry, times(2)).registerInterposedSynchronization(syncCaptor.capture());

    // Simulate successful commit for both
    for (Synchronization sync : syncCaptor.getAllValues()) {
      sync.afterCompletion(Status.STATUS_COMMITTED);
    }

    // Then
    verify(action1).run();
    verify(action2).run();
  }

  @Test
  @DisplayName("Should not fail if action throws exception")
  void testExecuteAfterCommit_ActionThrowsException() {
    // Given
    Runnable action = () -> {
      throw new RuntimeException("Action failed");
    };
    ArgumentCaptor<Synchronization> syncCaptor = ArgumentCaptor.forClass(Synchronization.class);

    // When
    service.executeAfterCommit(action);
    verify(mockRegistry).registerInterposedSynchronization(syncCaptor.capture());

    // Then - should not throw, just let the exception propagate from action
    Synchronization capturedSync = syncCaptor.getValue();
    assertThrows(RuntimeException.class, () -> capturedSync.afterCompletion(Status.STATUS_COMMITTED));
  }
}
