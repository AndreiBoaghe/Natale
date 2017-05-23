package org.vaadin.natale.dataprovider;

import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * {@link ListDataProvider} wrapper for some BackEnd data storage.
 *
 * @param <T> data type
 * @since 2.0
 */
public class ConfigurableBackEndDataProvider<T> extends ListDataProvider<T> {

	protected final Supplier<Collection<T>> getAllElementsMethod;
	protected Consumer<T> updateElementMethod;
	protected Consumer<T> deleteElementMethod;
	protected Consumer<T> addElementMethod;

	protected boolean isLazy = false;

	/**
	 * Constructs a new ConfigurableBackEndDataProvider with supplier method,
	 * that gets a collection of {@code T} elements.
	 * <p>
	 * Be aware, that data wouldn't be "updated" in backend, if other methods
	 * aren't set.
	 *
	 * @param getAllElementsMethod supplier, to obtain data from backend.
	 * @see #withAddElementMethod(Consumer)
	 * @see #withDeleteElementMethod(Consumer)
	 * @see #withUpdateElementMethod(Consumer)
	 */
	public ConfigurableBackEndDataProvider(Supplier<Collection<T>> getAllElementsMethod) {
		super(getAllElementsMethod.get());
		this.getAllElementsMethod = getAllElementsMethod;
	}

	/**
	 * Constructs a new ConfigurableBackEndDataProvider with all necessary supplier
	 * and consumer methods.
	 *
	 * @param getAllElementsMethod supplier, to obtain data from backend.
	 * @param updateElementMethod  consumer to update item in backend.
	 * @param deleteElementMethod  consumer to delete item in backend.
	 * @param addElementMethod     consumer to add item in backend.
	 */
	public ConfigurableBackEndDataProvider(Supplier<Collection<T>> getAllElementsMethod,
	                                       Consumer<T> updateElementMethod,
	                                       Consumer<T> deleteElementMethod,
	                                       Consumer<T> addElementMethod) {
		super(getAllElementsMethod.get());
		this.getAllElementsMethod = getAllElementsMethod;
		this.updateElementMethod = updateElementMethod;
		this.deleteElementMethod = deleteElementMethod;
		this.addElementMethod = addElementMethod;
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {
		if (!isLazy)
			getItemsFromBackend();

		return super.fetch(query);
	}

	@Override
	public void refreshItem(T item) {
		if (updateElementMethod != null)
			updateElementMethod.accept(item);
		fireEvent(new DataChangeEvent.DataRefreshEvent<>(this, item));
	}

	public void addItem(T item) throws IllegalArgumentException {
		if (addElementMethod != null)
			addElementMethod.accept(item);

		if (getItems().add(item)) {
			fireEvent(new DataChangeEvent<>(this));
		} else {
			throw new IllegalArgumentException("There is already the same object in current data list - " + item);
			//TODO
		}
	}

	public void deleteItem(T item) {
		if (deleteElementMethod != null)
			deleteElementMethod.accept(item);

		getItems().remove(item);
		fireEvent(new DataChangeEvent<>(this));
	}

	/**
	 * Set a consumer to update items in backend.
	 *
	 * @param updateElementMethod consumer to update item in backend.
	 * @return current configurableBackEndDataProvider
	 */
	public ConfigurableBackEndDataProvider<T> withUpdateElementMethod(Consumer<T> updateElementMethod) {
		this.updateElementMethod = updateElementMethod;
		return this;
	}

	@Override
	public void refreshAll() {
		getItemsFromBackend();
		fireEvent(new DataChangeEvent<>(this));
	}

	protected void getItemsFromBackend() {
		try {
			Collection<T> updatedItems = getAllElementsMethod.get();
			getItems().clear();
			getItems().addAll(updatedItems);
		} catch (Exception e) {
			e.printStackTrace();
			//TODO
		}
	}

	/**
	 * Set a consumer to delete items in backend.
	 *
	 * @param deleteElementMethod consumer to delete item in backend.
	 * @return current configurableBackEndDataProvider
	 */
	public ConfigurableBackEndDataProvider<T> withDeleteElementMethod(Consumer<T> deleteElementMethod) {
		this.deleteElementMethod = deleteElementMethod;
		return this;
	}

	/**
	 * Set a consumer to add new items in backend.
	 *
	 * @param addElementMethod consumer to update item in backend.
	 * @return current configurableBackEndDataProvider
	 */
	public ConfigurableBackEndDataProvider<T> withAddElementMethod(Consumer<T> addElementMethod) {
		this.addElementMethod = addElementMethod;
		return this;
	}

	public boolean isLazy() {
		return isLazy;
	}

	public void setLazy(boolean lazy) {
		isLazy = lazy;
	}

}
