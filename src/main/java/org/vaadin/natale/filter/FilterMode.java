package org.vaadin.natale.filter;

public enum FilterMode {

    // Use only for String type values.
    // If it used for other, then property value and filter value will be cast to <i>String</i>
    CONTAINS,

    EQUALS,

    GREATER,

    SMALLER,

    GREATER_OR_EQUAL,

    SMALLER_OR_EQUAL,

    NOT_CONTAINS,

    NOT_EQUALS
}
