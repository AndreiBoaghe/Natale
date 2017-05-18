package org.vaadin.natale.dataprovider;

import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JpaDataProvider<T> extends ListDataProvider<T> {

	private final static Logger logger = Logger.getLogger(JpaDataProvider.class);

	private final JpaRepository<T, ?> repository;
	private boolean isLazy = false;

	public JpaDataProvider(JpaRepository<T, ?> repository) {
		super(new LinkedHashSet<>(repository.findAll()));
		this.repository = repository;
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {

		if (!isLazy)
			getItemsFromBackend();

		return super.fetch(query);
	}

	@Override
	public void refreshItem(T item) {
		repository.save(item);
		fireEvent(new DataChangeEvent.DataRefreshEvent<>(this, item));
	}

	@Override
	public void refreshAll() {
		getItemsFromBackend();
		fireEvent(new DataChangeEvent<>(this));
	}

	public void deleteItem(T item) {
		repository.delete(item);
		getItems().remove(item);
		fireEvent(new DataChangeEvent<>(this));
	}

	public void addItem(T item) {
		repository.save(item);

		// Precaution... (maybe will be deleted in future)
		if (getItems().add(item)) {
			fireEvent(new DataChangeEvent<>(this));
		} else {
			logger.error("There is already the same object in current data list - " + item);
		}
	}

	private void getItemsFromBackend() {
		getItems().clear();
		getItems().addAll(repository.findAll());
	}

	private Sort getSorting(Query<T, SerializablePredicate<T>> query) {
		return new Sort(query.getSortOrders().stream()
				.map(sortOrder -> new Order(
						sortOrder.getDirection() == SortDirection.ASCENDING
								? Direction.ASC : Direction.DESC,
						sortOrder.getSorted()))
				.collect(Collectors.toList()));
	}

	public boolean isLazy() {
		return isLazy;
	}

	public void setLazy(boolean lazy) {
		isLazy = lazy;
	}
}
