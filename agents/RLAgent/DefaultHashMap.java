
public class DefaultHashMap<K,V> extends HashMap<K,V> {
  /* Returns defaultValue if the key does not exist */
  protected V defaultValue;
  public DefaultHashMap(V defaultValue) {
    this.defaultValue = defaultValue;
  }
  @Override
  public V get(Object k) {
    return containsKey(k) ? super.get(k) : defaultValue;
  }
}
