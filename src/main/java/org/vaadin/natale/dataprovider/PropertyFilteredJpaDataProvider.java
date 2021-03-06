package org.vaadin.natale.dataprovider;

import com.vaadin.server.SerializablePredicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vaadin.natale.filter.PropertyFilter;
import org.vaadin.natale.util.ObservableHashMap;

import java.util.Objects;

import static org.vaadin.natale.util.ReflectionUtil.getPropertyValueByName;

public class PropertyFilteredJpaDataProvider<T> extends JpaDataProvider<T> {

	// Mutable observable map of property filters.
	// Key - property name (PropertyFilter.getPropertyName()),
	// Value - PropertyFilter itself.
	private final ObservableHashMap<String, PropertyFilter> filterMap;

	public PropertyFilteredJpaDataProvider(JpaRepository<T, ?> repository) {
		super(repository);
		filterMap = new ObservableHashMap<>();

		// Register a property listener to handle simple changes (put/remove) in map.
		filterMap.addPropertyChangeListener(event -> updateMainFilterObject());
	}

	/**
	 * Sets a filter to be applied to all queries. The filter replaces any
	 * filter that has been set or added previously.
	 *
	 * @param filter the filter to set, or <code>null</code> to remove any set filters
	 */
	@SuppressWarnings("unchecked")
	public void setPropertyFilter(PropertyFilter filter) {
		if (filter == null) {
			filterMap.clear();
			super.setFilter(null);
		} else {
			filterMap.putIfAbsent(filter.getPropertyName(), filter);
			setFilter(entity -> filter.testProperty(getPropertyValueByName(filter.getPropertyName(), entity)));

			// Register a property listener to handle any changes in filter object.
			filter.addPropertyChangeListener(event -> updateMainFilterObject());
		}
	}

	/**
	 * Adds a property filter to be applied to all queries. The filter will be used in
	 * addition to any filter that has been set or added previously.
	 *
	 * @param filter the filter to add, not {@code null}
	 * @see #setPropertyFilter(PropertyFilter)
	 * @see #getPropertyFilterByPropertyName(String)
	 */
	@SuppressWarnings("unchecked")
	public void addPropertyFilter(PropertyFilter filter) {
		Objects.requireNonNull(filter, "Filter cannot be null");
		setPropertyFilter(filter);
	}

	/**
	 * Get PropertyFilter by specified property name.<br>
	 *
	 * @param propertyName - property name.
	 * @return PropertyFilter by specified property name.
	 * @throws NullPointerException if no any {@code PropertyFilter} founded.
	 */
	public PropertyFilter getPropertyFilterByPropertyName(String propertyName) {
		PropertyFilter filter = filterMap.get(propertyName);

		Objects.requireNonNull(filter, "No any PropertyFilter founded by property name: " + propertyName);
		return filter;
	}

	/**
	 * Remove PropertyFilter by specified property name.<br>
	 *
	 * @param propertyName - property name.
	 * @return the previous PropertyFilter object associated with specified property name.
	 */
	public PropertyFilter removePropertyFilterByPropertyName(String propertyName) {
		return filterMap.remove(propertyName);
	}

	@SuppressWarnings("unchecked")
	private void updateMainFilterObject() {
		setFilter(null);
		filterMap.forEach((propertyName, filter) ->
				addFilter(entity -> filter.testProperty(getPropertyValueByName(filter.getPropertyName(), entity))));
	}

}
