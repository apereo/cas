package org.apereo.inspektr.audit.support;

import module java.base;

/**
 * Interface describing match criteria in terms of a SQL select clause.
 *
 * @author Middleware
 * @since 1.0
 */
@FunctionalInterface
public interface WhereClauseMatchCriteria {

    /**
     * Gets parameter values.
     *
     * @return Immutable list of parameter values for a parameterized query or
     * an empty list if the where clause is not parameterized.
     */
    List<?> getParameterValues();
}
