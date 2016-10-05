package fi.thl.termed.service;

import com.google.common.base.Optional;

import java.io.Serializable;
import java.util.List;

import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.SpecificationQuery;

/**
 * Generic interface for services.
 */
public interface Service<K extends Serializable, V> {

  /**
   * Save (insert or update) values with dependencies.
   */
  void save(List<V> values, User currentUser);

  void save(V value, User currentUser);

  /**
   * Delete value (with dependencies) by id.
   */
  void delete(K id, User currentUser);

  /**
   * Get specified values. Values are expected to be fully populated.
   */
  List<V> get(SpecificationQuery<K, V> specification, User currentUser);

  /**
   * Get values by ids. Values are expected to be fully populated.
   */
  List<V> get(List<K> ids, User currentUser);

  /**
   * Get value by id. Value is expected to be fully populated.
   */
  Optional<V> get(K id, User currentUser);

}
