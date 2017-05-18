package org.vaadin.natale.components;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Grid;
import org.apache.log4j.Logger;
import org.vaadin.natale.util.PropertyNameFormatter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.vaadin.natale.util.ReflectionUtil.invokeGetterMethodByPropertyName;


public class NGrid<T> extends Grid<T> {

    private static final Logger logger = Logger.getLogger(NGrid.class);

    private T lastModifiedItem;

    public NGrid(Class<T> beanClazz) {
        super(beanClazz);
    }

    public NGrid(Class<T> beanClazz, DataProvider<T, ?> dataProvider) {
        super(beanClazz);
        //grid.setStyleName("good");
        setDataProvider(dataProvider);
        setSelectionMode(SelectionMode.SINGLE);

//        addItemClickListener(new ItemClickListener<T>() {
//            @Override
//            public void itemClick(ItemClick<T> event) {
//                if (event.getMouseEventDetails().isDoubleClick()) {
//                    lastModifiedItem = event.getItem();
//
//                    setStyleGenerator(new StyleGenerator<T>() {
//                        @Override
//                        public String apply(T item) {
//
//                            if (item.equals(lastModifiedItem))
//                                return "good2";
//                            return null;
//                        }
//                    });
//                }
//            }
//        });

    }

    /**
     * Wrap current NGrid and create (set Captions and Id) columns.<br>
     *
     * @param propertiesNames - properties names for Entity class.
     * @return NGrid.<br>
     * <b>Note:</b> Columns id are generated accordingly to columnIdsList.
     */
    public NGrid<T> withProperties(String... propertiesNames) {
        PropertyNameFormatter propertyNameFormatter = new PropertyNameFormatter();

        // Remove all columns and add the manually.
        removeAllColumns();

        for (String propertyName : propertiesNames) {
            addColumn((ValueProvider<T, Object>) t -> invokeGetterMethodByPropertyName(propertyName, t))
                    .setCaption(propertyNameFormatter.getConvertedPropertyName(propertyName))
                    .setId(propertyName);
        }

        return this;
    }

    /**
     * Wrap current NGrid and set the captions to <b>ALL</b> columns.
     *
     * @param columnHeaders - captions to set for columns.
     * @return NGrid
     */
    public NGrid<T> withColumnsHeaders(String... columnHeaders) {
        List<String> columnIdsList = getColumns().stream()
                .map(tColumn -> tColumn.getId())
                .collect(Collectors.toList());

        if (columnIdsList.size() != columnHeaders.length) {
            logger.error("ERROR during add columns headers");
            logger.error("No match column headers for all column Id's");
            logger.error("ColumnIdList size = " + columnIdsList.size() + ", but founded " + columnHeaders.length + " column header parameters.");
            return this;
        }

        for (int i = 0; i < columnHeaders.length; ++i)
            getColumn(columnIdsList.get(i)).setCaption(columnHeaders[i]);

        return this;
    }

    /**
     * Wrap current NGrid and set columns to be hidable.
     *
     * @param columnIds - column id's to be set as 'Hidable'.
     * @return NGrid
     */
    public NGrid<T> withHidableColumns(String... columnIds) {

        Arrays.asList(columnIds).stream()
                .forEach(columnId -> {
                    try {
                        getColumn(columnId).setHidable(true);
                    } catch (NullPointerException e) {
                        logger.error("ERROR during set hidable columns!");
                        logger.error("No column id [" + columnId + "] found in NGrid of class [" + getBeanType().getSimpleName() + "]");
                        e.printStackTrace();
                    }
                });

        return this;
    }

    /**
     * Wrap current NGrid and hide columns by column id.
     *
     * @param columnIds - column id's to hide.
     * @return NGrid
     */
    public NGrid<T> withHidenColumns(String... columnIds) {

        List<String> columnIdsList = getColumns().stream()
                .map(tColumn -> tColumn.getId())
                .collect(Collectors.toList());

        if (columnIdsList.size() < columnIds.length) {
            logger.error("ERROR during set hiden columns");
            logger.error("ColumnIdList size = " + columnIdsList.size() + ", but founded " + columnIds.length + " column is's parameters.");
            return this;
        }

        Arrays.asList(columnIds).stream()
                .forEach(columnId -> {
                    try {
                        getColumn(columnId).setHidden(true);
                    } catch (NullPointerException e) {
                        logger.error("ERROR during hide columns!");
                        logger.error("No column id [" + columnId + "] found in NGrid of class [" + getBeanType().getSimpleName() + "]");
                        e.printStackTrace();
                    }
                });

        return this;
    }

    /**
     * Wrap current NGrid and set column order, using column Ids.
     *
     * @param columnIds - order of columns.
     * @return NGrid
     */
    public NGrid<T> withColumnOrder(String... columnIds) {
        try {
            setColumnOrder(columnIds);
        } catch (IllegalStateException | NullPointerException e) {
            logger.error("ERROR during set column order!");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Get a Map with all grid columns details.
     * Key - column Caption.
     * Value - column id.
     *
     * @return Map
     */
    public Map<String, String> getColumnCaptionToColumnIdMap() {
        Map<String, String> columnIdCaptionMap = new LinkedHashMap<>();

        getColumns().stream().forEach(column -> {
            columnIdCaptionMap.put(column.getCaption(), column.getId());
        });

        return columnIdCaptionMap;
    }

}
