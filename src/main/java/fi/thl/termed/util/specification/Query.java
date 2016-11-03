package fi.thl.termed.util.specification;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public class Query<K extends Serializable, V> {

  private Specification<K, V> specification;
  private List<String> orderBy;
  private int max;
  private Engine engine;

  public Query(Specification<K, V> specification) {
    this(specification, Lists.<String>newArrayList(), Integer.MAX_VALUE, Engine.ANY);
  }

  public Query(Specification<K, V> specification, Engine engine) {
    this(specification, Lists.<String>newArrayList(), Integer.MAX_VALUE, engine);
  }

  public Query(Specification<K, V> specification, List<String> orderBy, int max,
               Engine engine) {
    this.specification = specification;
    this.orderBy = orderBy;
    this.max = max;
    this.engine = engine;
  }

  public Specification<K, V> getSpecification() {
    return specification;
  }

  public List<String> getOrderBy() {
    return orderBy;
  }

  public int getMax() {
    return max;
  }

  public Engine getEngine() {
    return engine;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("specification", specification)
        .add("orderBy", orderBy)
        .add("max", max)
        .add("engine", engine)
        .toString();
  }

  public enum Engine {
    SQL, LUCENE, ANY
  }

}
