package org.vaadin.natale.util;

import java.beans.PropertyChangeListener;

public interface PropertyChangeNotification {
	void addPropertyChangeListener(PropertyChangeListener listener);
	void removePropertyChangeListener(PropertyChangeListener listener);
}