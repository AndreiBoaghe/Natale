package org.vaadin.natale.filter;

public interface Filter<ENTITY, PROPERTY> {

	boolean testProperty(PROPERTY object);

	boolean testEntity(ENTITY object);
}
