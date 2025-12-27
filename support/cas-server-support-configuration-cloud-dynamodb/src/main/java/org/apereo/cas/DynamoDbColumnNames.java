package org.apereo.cas;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link DynamoDbColumnNames}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@RequiredArgsConstructor
public enum DynamoDbColumnNames {
    /**
     * Column name.
     */
    NAME("name"),

    /**
     * Column value.
     */
    VALUE("value");

    private final String columnName;
}
