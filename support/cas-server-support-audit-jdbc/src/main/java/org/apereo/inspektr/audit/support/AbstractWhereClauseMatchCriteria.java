package org.apereo.inspektr.audit.support;

import java.util.List;

/**
 * Match criteria defined in terms of a SQL where clause limiter.  The
 * {@link #toString()} method of this class produces a where clause beginning
 * with the text "WHERE" such that is can be directly appended to a SQL
 * statement without a where clause to narrow results.
 *
 * @author Marvin S. Addison
 * @since 1.0
 */
public abstract class AbstractWhereClauseMatchCriteria implements WhereClauseMatchCriteria {

    protected StringBuilder sbClause = new StringBuilder();
    
    @Override
    public String toString() {
        return this.sbClause.toString();
    }

    /**
     * Adds a parameterized selection criterion of the form "column [operator] ?".
     * to the where clause.
     *
     * @param column Database column name.
     * @param operator the operator to use to separate.
     */
    protected void addCriteria(final String column, final String operator) {
        if (this.sbClause.isEmpty()) {
            this.sbClause.append("WHERE");
        } else {
            this.sbClause.append(" AND");
        }
        this.sbClause.append(' ');
        this.sbClause.append(column);
        this.sbClause.append(' ');
        this.sbClause.append(operator);
        this.sbClause.append(" ?");
    }

    @Override
    public List<?> getParameterValues() {
        return List.of();
    }
}
