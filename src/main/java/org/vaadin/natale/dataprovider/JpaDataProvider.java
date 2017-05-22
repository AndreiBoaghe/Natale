package org.vaadin.natale.dataprovider;

import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import org.apache.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.LinkedHashSet;
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

	public boolean isLazy() {
		return isLazy;
	}

	public void setLazy(boolean lazy) {
		isLazy = lazy;
	}
}
