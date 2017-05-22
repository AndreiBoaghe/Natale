package org.vaadin.natale.filter;

import org.apache.log4j.Logger;
import org.vaadin.natale.util.PropertyChangeNotification;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.function.BiFunction;

public class PropertyFilter<P> implements PropertyChangeNotification {

	private final static Logger logger = Logger.getLogger(PropertyFilter.class);

	private PropertyChangeSupport changer = new PropertyChangeSupport(this);

	private final String propertyName;

	private P filterValue;

	private final BiFunction<P, P, Integer> compareToMethod;

	private FilterMode mode = FilterMode.EQUALS;

	private boolean ignoreCase = true;

	private PropertyFilter(String propertyName, BiFunction<P, P, Integer> compareToMethod) {
		this.propertyName = propertyName;
		this.compareToMethod = compareToMethod;
	}

	private PropertyFilter(String propertyName, P filterValue, BiFunction<P, P, Integer> compareToMethod) {
		this.propertyName = propertyName;
		this.filterValue = filterValue;
		this.compareToMethod = compareToMethod;
	}

	/**
	 * Creates a new PropertyFilter for {@code String} type property.
	 *
	 * @param propertyName       property name. <br>
	 *                           Specified property must be an instance of String class.
	 *                           Or according getter method have to transform it to String.
	 * @param initialFilterValue string type initial filter value. (or {@code null})
	 * @param <P>                String
	 * @return PropertyFilter for {@code String} type property.
	 */
	public static <P extends String> PropertyFilter<P> build(String propertyName, P initialFilterValue) {
		return new PropertyFilter<>(propertyName, initialFilterValue, String::compareTo);
	}

	/**
	 * Creates a new PropertyFilter for {@code Number} type property.
	 *
	 * @param propertyName       property name. <br>
	 *                           Specified property must be an instance of class, that extends {@code Number} class
	 *                           Or according getter method have to transform it to Number.
	 * @param initialFilterValue number type initial filter value. (or {@code null})
	 * @param <P>                class extends the {@code Number} class
	 * @return PropertyFilter for {@code Number} type property.
	 */
	public static <P extends Number> PropertyFilter<P> build(String propertyName, P initialFilterValue) {
		return new PropertyFilter<>(
				propertyName,
				initialFilterValue,
				(t, t2) -> Double.compare(t.doubleValue(), t2.doubleValue()));
	}

	/**
	 * Creates a new PropertyFilter for specified property type.
	 *
	 * @param propertyName    property name
	 * @param compareToMethod a function that accepts two {@code <T>} arguments and return int.<br>
	 * @param <T>             property type
	 * @return PropertyFilter for {@code <T>} type property.
	 */
	public static <T> PropertyFilter<T> build(String propertyName, BiFunction<T, T, Integer> compareToMethod) {
		return new PropertyFilter<>(propertyName, null, compareToMethod);
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
	public boolean testProperty(Object propertyValue) {
		if (filterValue == null || propertyValue == null)
			return true;

		P castedPropertyValue;
		try {
			castedPropertyValue = castPropertyValue(propertyValue);
		} catch (ClassCastException e) {
			return false;
		}

		boolean filterPestResult = false;
		switch (mode) {
			case CONTAINS:
				filterPestResult = ignoreCase ?
						castedPropertyValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase())
						: castedPropertyValue.toString().contains(filterValue.toString());
				break;
			case NOT_CONTAINS:
				filterPestResult = ignoreCase ?
						castedPropertyValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase())
						: castedPropertyValue.toString().contains(filterValue.toString());
				filterPestResult = !filterPestResult;
				break;
			case EQUALS:
				// 'EQUALS' can be used as for string, as for other type objects. So need to check ignoreCase flag.
				if (castedPropertyValue instanceof String) {
					filterPestResult = ignoreCase ?
							castedPropertyValue.toString().toLowerCase().equals(filterValue.toString().toLowerCase())
							: castedPropertyValue.toString().equals(filterValue.toString());
				} else {
					filterPestResult = castedPropertyValue.equals(filterValue);
				}
				break;
			case NOT_EQUALS:
				// Same as 'EQUALS', ... need to check ignoreCase flag.
				if (castedPropertyValue instanceof String) {
					filterPestResult = ignoreCase ?
							castedPropertyValue.toString().toLowerCase().equals(filterValue.toString().toLowerCase())
							: castedPropertyValue.toString().equals(filterValue.toString());
				} else {
					filterPestResult = castedPropertyValue.equals(filterValue);
				}
				filterPestResult = !filterPestResult;
				break;
			case GREATER:
				filterPestResult = compareToMethod.apply(castedPropertyValue, filterValue) > 0;
				break;
			case SMALLER:
				filterPestResult = compareToMethod.apply(castedPropertyValue, filterValue) < 0;
				break;
			case GREATER_OR_EQUAL:
				filterPestResult = compareToMethod.apply(castedPropertyValue, filterValue) >= 0;
				break;
			case SMALLER_OR_EQUAL:
				filterPestResult = compareToMethod.apply(castedPropertyValue, filterValue) <= 0;
				break;
		}

		return filterPestResult;
	}


	@SuppressWarnings("unchecked")
	private P castPropertyValue(Object propertyValue) {
		P castedPropertyValue = null;
		try {
			castedPropertyValue = (P) propertyValue;
		} catch (ClassCastException e) {
			logger.error("Actual property value class -" + propertyValue.getClass()
					+ " does not meet current filter value class - " + filterValue.getClass());
		}
		return castedPropertyValue;
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

	public PropertyFilter<P> setFilterValue(P filterValue) {
		changer.firePropertyChange("filterValue", this.filterValue, filterValue);
		this.filterValue = filterValue;
		return this;
	}

	public PropertyFilter<P> setFilterMode(FilterMode mode) {
		changer.firePropertyChange("filterMode", this.mode, mode);
		this.mode = mode;

		checkPropertyFilterModeUsing();
		return this;
	}

	public PropertyFilter<P> setIgnoreCase(boolean ignoreCase) {
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

	/**
	 * Checks {@code FilterMode} and {@code filterValue} object class.<br>
	 * Logs a warn, if filter hadn't passed the check.<br>
	 * <b>Note: </b>'CONTAINS' and 'NOT_CONTAINS' modes are used only for string objects.
	 */
	private void checkPropertyFilterModeUsing() {
		if ((mode.equals(FilterMode.CONTAINS) || mode.equals(FilterMode.NOT_CONTAINS)) &&
				(!filterValue.getClass().equals(String.class))) {
			logger.warn("You change FilterMode to " + mode + ", which is used only for String values. " +
					"Current filterValue - " + filterValue + "(" + filterValue.getClass() + ") and received property values would be casted to String.\n" +
					"Check filterValue, or change FilterMode to another, or change PropertyFilter generic parameter <P> to avoid this warning.");
		}
	}
}