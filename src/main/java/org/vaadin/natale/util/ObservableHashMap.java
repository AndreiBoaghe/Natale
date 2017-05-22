package org.vaadin.natale.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

public class ObservableHashMap<K, V> extends HashMap<K, V> implements PropertyChangeNotification {

	private PropertyChangeSupport changer = new PropertyChangeSupport(this);

	public V put(K key, V value) {
		changer.firePropertyChange(String.valueOf(key), value, get(key));
		return super.put(key, value);
	}

	public V remove(Object key) {
		changer.firePropertyChange(String.valueOf(key), get(key), null);
		return super.remove(key);
	}

	public boolean remove(Object key, Object value) {
		changer.firePropertyChange(String.valueOf(key), get(key), null);
		return super.remove(key, value);
	}

	public V putIfAbsent(K key, V value) {
		changer.firePropertyChange(String.valueOf(key), value, get(key));
		return super.put(key, value);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changer.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changer.removePropertyChangeListener(listener);
	}
}
