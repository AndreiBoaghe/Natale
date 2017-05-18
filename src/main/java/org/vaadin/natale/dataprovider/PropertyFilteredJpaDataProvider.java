package org.vaadin.natale.dataprovider;

import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import org.apache.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vaadin.natale.filter.impl.PropertyFilter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.vaadin.natale.util.ReflectionUtil.invokeGetterMethodByPropertyName;

public class PropertyFilteredJpaDataProvider<T> extends JpaDataProvider<T> {

	private final static Logger logger = Logger.getLogger(PropertyFilteredJpaDataProvider.class);

	// Map of ReflectionFilters for "Multiple filtering" the data.
	// Key - property name (PropertyFilter.getPropertyName()),
	// Value - PropertyFilter itself.
	private final Map<String, PropertyFilter> filterMap;

	public PropertyFilteredJpaDataProvider(JpaRepository<T, ?> repository) {
		super(repository);
		filterMap = new LinkedHashMap<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {


		return super.fetch(query);
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
			super.setFilter(new SerializablePredicate<T>() {
				@Override
				public boolean test(T t) {
					return filter.testProperty(t);
				}
			});

			// Convert Map of ReflectionFilters to "real" filter - SerializablePredicate<T> filter.
			filterMap.forEach((propertyName, propertyFilter) -> addFilter(objectToFilter ->
					propertyFilter.testProperty(invokeGetterMethodByPropertyName(propertyFilter.getPropertyName(), objectToFilter))));

		}
	}

	/**
	 * Adds a property filter to be applied to all queries. The filter will be used in
	 * addition to any filter that has been set or added previously.
	 *
	 * @param filter the filter to add, not <code>null</code>
	 * @see #setPropertyFilter(PropertyFilter)
	 * @see #getPropertyFilterByPropertyName(String)
	 */
	public void addPropertyFilter(PropertyFilter filter) {
		Objects.requireNonNull(filter, "Filter cannot be null");
		setPropertyFilter(filter);
	}

	/**
	 * Get PropertyFilter by specified property name.<br>
	 *
	 * @param propertyName - property name.
	 * @return PropertyFilter for propertyName.
	 */
	public PropertyFilter getPropertyFilterByPropertyName(String propertyName) {
		PropertyFilter filter = filterMap.get(propertyName);
		if (filter == null) {
			logger.error("No any PropertyFilter found by property name: " + propertyName);
			//TODO: throw exception????
		}
		return filter;
	}

}
