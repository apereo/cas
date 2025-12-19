package org.apereo.cas.jdbc;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link PartialWhereClause}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString
@Getter
@Setter
public class PartialWhereClause {
    private StringBuilder sql = new StringBuilder();
    private List<String> arguments = new ArrayList<>();
}
