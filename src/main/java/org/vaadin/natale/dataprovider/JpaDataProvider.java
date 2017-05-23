package org.vaadin.natale.dataprovider;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Creates a new ConfigurableBackEndDataProvider for JpaRepository.
 *
 * @param <T> data type
 */
public class JpaDataProvider<T> extends ConfigurableBackEndDataProvider<T> {

	public JpaDataProvider(JpaRepository<T, ?> repository) {
		super(repository::findAll, repository::save, repository::delete, repository::save);
	}
}
