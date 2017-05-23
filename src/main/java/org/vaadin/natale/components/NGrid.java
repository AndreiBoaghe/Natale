package org.vaadin.natale.components;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;
import org.apache.log4j.Logger;
import org.vaadin.natale.util.PropertyNameFormatter;

import javax.naming.SizeLimitExceededException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.vaadin.natale.util.ReflectionUtil.getPropertyValueByName;

/**
 * A better typed version of the {@link Grid} component in Vaadin.<br>
 * Allows user manually add nested property as columns and simplify<br>
 * Grid creation.
 *
 * @param <T> data type
 */
public class NGrid<T> extends Grid<T> {

	private static final Logger logger = Logger.getLogger(NGrid.class);

	public NGrid(Class<T> beanClazz) {
		super(beanClazz);
	}

	public NGrid(Class<T> beanClazz, DataProvider<T, ?> dataProvider) {
		super(beanClazz);
		setDataProvider(dataProvider);
	}

	/**
	 * Wrap current NGrid and create (set Captions and Id) columns.<br>
	 * <b>Note:</b> Columns id are generated accordingly to columnIdsList.<br>
	 * It means, column.id = propertyName.
	 *
	 * @param propertiesNames properties names of {@code <T>} class.
	 * @return current NGrid.
	 */
	public NGrid<T> withColumns(String... propertiesNames) {
		PropertyNameFormatter propertyNameFormatter = new PropertyNameFormatter();

		// Remove all columns and add them manually.
		removeAllColumns();

		for (String propertyName : propertiesNames) {
			addColumn((ValueProvider<T, Object>) t -> getPropertyValueByName(propertyName, t))
					.setCaption(propertyNameFormatter.getConvertedPropertyName(propertyName))
					.setId(propertyName);
		}

		return this;
	}

	/**
	 * Wrap current NGrid and set the captions to <u>all the columns</u>.
	 *
	 * @param columnHeaders captions to set for columns.
	 * @return current NGrid
	 */
	public NGrid<T> withColumnsHeaders(String... columnHeaders) {
		List<String> columnIdsList = getColumns().stream()
				.map(Column::getId)
				.collect(Collectors.toList());

		if (columnIdsList.size() != columnHeaders.length) {
			String errorMessage = "No match column headers for all column Id's" +
					"ColumnIdList size = " + columnIdsList.size() + ", but founded " + columnHeaders.length + " column header parameters.";
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		for (int i = 0; i < columnHeaders.length; ++i)
			getColumn(columnIdsList.get(i)).setCaption(columnHeaders[i]);

		return this;
	}

	/**
	 * Wrap current NGrid and set columns to be hidable.
	 *
	 * @param columnIds column id's to be set as 'Hidable'.
	 * @return current NGrid
	 */
	public NGrid<T> withHidableColumns(String... columnIds) {

		Arrays.asList(columnIds).forEach(columnId -> {
			try {
				getColumn(columnId).setHidable(true);
			} catch (NullPointerException e) {
				logColumnNotFoundError(columnId);
			}
		});

		return this;
	}

	/**
	 * Wrap current NGrid and hide columns by column id.
	 *
	 * @param columnIds column id's to hide.
	 * @return current NGrid
	 */
	public NGrid<T> withHiddenColumns(String... columnIds) {

		List<String> columnIdsList = getColumns().stream()
				.map(Column::getId)
				.collect(Collectors.toList());

		if (columnIdsList.size() < columnIds.length) {
			String errorMessage = "ColumnIdList size = " + columnIdsList.size() + ", but founded " + columnIds.length + " column is's parameters.";
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		Arrays.asList(columnIds).forEach(columnId -> {
			try {
				getColumn(columnId).setHidden(true);
			} catch (NullPointerException e) {
				logColumnNotFoundError(columnId);
			}
		});

		return this;
	}

	/**
	 * Wrap current NGrid and set column order, using column Ids.
	 *
	 * @param columnIds order of columns.
	 * @return current NGrid
	 */
	public NGrid<T> withColumnOrder(String... columnIds) {
		try {
			setColumnOrder(columnIds);
		} catch (IllegalStateException | NullPointerException e) {
			e.printStackTrace();
		}
		return this;
	}

	private void logColumnNotFoundError(String columnId) {
		String errorMessage = "No column id [" + columnId + "] found in NGrid of class [" + getBeanType().getSimpleName() + "]";
		logger.error(errorMessage);
		throw new NullPointerException(errorMessage);
	}

}
