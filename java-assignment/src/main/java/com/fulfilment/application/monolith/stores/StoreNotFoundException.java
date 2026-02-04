package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.exceptions.EntityNotFoundException;

/**
 * Exception thrown when a store cannot be found by its ID.
 */
public class StoreNotFoundException extends EntityNotFoundException {

  private final Long storeId;

  public StoreNotFoundException(Long storeId) {
    super("Store with id of " + storeId + " does not exist.");
    this.storeId = storeId;
  }

  public Long getStoreId() {
    return storeId;
  }
}
