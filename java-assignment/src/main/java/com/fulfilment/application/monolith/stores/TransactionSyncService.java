package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * Service for executing callbacks after successful transaction commits.
 *
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * @Inject TransactionSyncService syncService;
 *
 * @POST
 * @Transactional
 * public Response create(Store store) {
 *     store.persist();
 *     syncService.executeAfterCommit(() ->
 *         legacySystem.notifyStoreCreated(store)
 *     );
 *     return Response.ok(store).build();
 * }
 * }</pre>
 */
@ApplicationScoped
public class TransactionSyncService {

  @Inject TransactionSynchronizationRegistry syncRegistry;

  /**
   * Executes the given action AFTER the current transaction commits successfully.

   * @param action Callback to execute after successful commit. Should be idempotent and handle its
   *     own exceptions, as failures cannot affect the committed transaction.
   * @throws IllegalStateException if called outside a transactional context
   */
  public void executeAfterCommit(Runnable action) {
    syncRegistry.registerInterposedSynchronization(
        new Synchronization() {
          @Override
          public void beforeCompletion() {
            // No action needed before completion
          }

          @Override
          public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
              action.run();
            }
          }
        });
  }
}
