package org.vaadin.natale.filter.impl;

import org.vaadin.natale.filter.Filter;

import java.util.Objects;
import java.util.function.Function;

public class EntityFilter<E> implements Filter<E> {

	private Function<E, Integer> testFunction;

	private EntityFilter(Function<E, Integer> testFunction) {
		this.testFunction = testFunction;
	}

	public static <E> EntityFilter<E> build(Function<E, Integer> testFunction) {
		return new EntityFilter<>(testFunction);
	}

	public boolean testProperty(E propertyValue) {
		Objects.requireNonNull(propertyValue, "Filter cannot testProperty null");

		return testFunction.apply(propertyValue) == 0;
	}

	public Function<E, Integer> getTestFunction() {
		return testFunction;
	}

	public void setTestFunction(Function<E, Integer> testFunction) {
		this.testFunction = testFunction;
	}
}
