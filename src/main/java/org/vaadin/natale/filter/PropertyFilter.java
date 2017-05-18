package org.vaadin.natale.filter;

import org.apache.log4j.Logger;
import org.vaadin.natale.util.PropertyChangeNotification;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.vaadin.natale.util.ReflectionUtil.invokeGetterMethodByPropertyName;

public class PropertyFilter<E, P> implements Filter<E, P>, PropertyChangeNotification {

	private final static Logger logger = Logger.getLogger(PropertyFilter.class);

	private PropertyChangeSupport changer = new PropertyChangeSupport(this);

	private final String propertyName;

	private P filterValue;

	private final BiFunction<P, P, Integer> compareToMethod;

	private FilterMode mode = FilterMode.EQUALS;

	private boolean ignoreCase = true;

	public PropertyFilter(String propertyName, BiFunction<P, P, Integer> compareToMethod) {
		this.propertyName = propertyName;
		this.compareToMethod = compareToMethod;
	}

	public PropertyFilter(String propertyName, P filterValue, BiFunction<P, P, Integer> compareToMethod) {
		this.propertyName = propertyName;
		this.filterValue = filterValue;
		this.compareToMethod = compareToMethod;
	}

	/**
	 * The main method of property configurable filtering process. <br>
	 * Compare the current {@code filterValue} with {@code propertyValue}, according to current filter parameters.
	 *
	 * @param propertyValue property value that would be filtered.
	 * @return {@code true} if {@code 'propertyValue'} pass the testProperty.<br>
	 * {@code false} if {@code 'propertyValue'} failed the testProperty.
	 */
	@SuppressWarnings("unchecked")
	public boolean testProperty(P propertyValue) {
		Objects.requireNonNull(propertyValue, "Filter cannot test null");
		Boolean filterPestResult = false;

		checkReflectionFilterModeUsing(propertyValue);

		// If filter value isn't set, it means the propertyValue pass the test.
		if (filterValue == null)
			return true;

		switch (mode) {
			case CONTAINS:
				filterPestResult = ignoreCase ?
						propertyValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase())
						: propertyValue.toString().contains(filterValue.toString());
				break;
			case NOT_CONTAINS:
				filterPestResult = ignoreCase ?
						propertyValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase())
						: propertyValue.toString().contains(filterValue.toString());
				filterPestResult = !filterPestResult;
				break;
			case EQUALS:
				// 'EQUALS' can be used as for string, as for other type objects. So need to check ignoreCase flag.
				if (propertyValue instanceof String) {
					filterPestResult = ignoreCase ?
							propertyValue.toString().toLowerCase().equals(filterValue.toString().toLowerCase())
							: propertyValue.toString().equals(filterValue.toString());
				} else {
					filterPestResult = propertyValue.equals(filterValue);
				}
				break;
			case NOT_EQUALS:
				// Same as 'EQUALS', ... need to check ignoreCase flag.
				if (propertyValue instanceof String) {
					filterPestResult = ignoreCase ?
							propertyValue.toString().toLowerCase().equals(filterValue.toString().toLowerCase())
							: propertyValue.toString().equals(filterValue.toString());
				} else {
					filterPestResult = propertyValue.equals(filterValue);
				}
				filterPestResult = !filterPestResult;
				break;
			case GREATER:
				filterPestResult = compareToMethod.apply(propertyValue, filterValue) > 0;
				break;
			case SMALLER:
				filterPestResult = compareToMethod.apply(propertyValue, filterValue) < 0;
				break;
			case GREATER_OR_EQUAL:
				filterPestResult = compareToMethod.apply(propertyValue, filterValue) >= 0;
				break;
			case SMALLER_OR_EQUAL:
				filterPestResult = compareToMethod.apply(propertyValue, filterValue) <= 0;
				break;
		}

		return filterPestResult;
	}

	@SuppressWarnings("unchecked")
	public boolean testEntity(E entity) {
		P propertyValue = null;
		try {
			propertyValue = (P) invokeGetterMethodByPropertyName(propertyName, entity);
		} catch (ClassCastException e) {
			logger.error("Actual property value class (" + invokeGetterMethodByPropertyName(propertyName, entity).getClass()
					+ ") of entity - " + entity + " does not meet current filter value class - " + filterValue.getClass());
		}
		return testProperty(propertyValue);
	}


	/**
	 * Checks {@code ReflectionFilterMode} and {@code objectPoFilter} class.<br>
	 * <b>Note: </b>'CONTAINS' and 'NOT_CONTAINS' modes are used only for string objects.
	 *
	 * @param objectPoFilter object to filter
	 */
	private void checkReflectionFilterModeUsing(P objectPoFilter) {
		if ((mode.equals(FilterMode.CONTAINS) || mode.equals(FilterMode.NOT_CONTAINS)) &&
				(!objectPoFilter.getClass().equals(String.class))) {
			logger.warn("Comparing current filter value [" + filterValue + "] with filter objects [" + objectPoFilter + "]"
					+ " like strings, cause current ReflectionFilterMode = " + mode + " (this mode is used only for string objects)");
			logger.warn("Change ReflectionFilterMode if it's necessary, or check objectPoFilter class (it's current class - " + objectPoFilter.getClass());
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public BiFunction<P, P, Integer> getCompareToMethod() {
		return compareToMethod;
	}

	public P getFilterValue() {
		return filterValue;
	}

	public FilterMode getMode() {
		return mode;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public PropertyFilter<E, P> setFilterValue(P filterValue) {
		changer.firePropertyChange("filterValue", this.filterValue, filterValue);
		this.filterValue = filterValue;
		return this;
	}

	public PropertyFilter<E, P> setFilterMode(FilterMode mode) {
		changer.firePropertyChange("filterMode", this.mode, mode);
		this.mode = mode;
		return this;
	}

	public PropertyFilter<E, P> setIgnoreCase(boolean ignoreCase) {
		changer.firePropertyChange("ignoreCase", this.ignoreCase, ignoreCase);
		this.ignoreCase = ignoreCase;
		return this;
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