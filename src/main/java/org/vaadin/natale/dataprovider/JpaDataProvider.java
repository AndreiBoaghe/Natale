package org.vaadin.natale.dataprovider;

import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import org.apache.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

public class JpaDataProvider<T> extends ListDataProvider<T> {

	private final static Logger logger = Logger.getLogger(JpaDataProvider.class);

	protected final JpaRepository<T, ?> repository;
	protected boolean isLazy = false;

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

	public void deleteItem(T item) {
		repository.delete(item);
		getItems().remove(item);
		fireEvent(new DataChangeEvent<>(this));
	}

	public void addItem(T item) {
		repository.save(item);

		// Precaution...
		if (getItems().add(item)) {
			fireEvent(new DataChangeEvent<>(this));
		} else {
			logger.error("There is already the same object in current data list - " + item);
		}
	}

	@Override
	public void refreshAll() {
		getItemsFromBackend();
		fireEvent(new DataChangeEvent<>(this));
	}

	private void getItemsFromBackend() {
		getItems().clear();
		getItems().addAll(repository.findAll());
	}
}
