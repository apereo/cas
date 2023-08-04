package org.apereo.inspektr.audit.support;

/**
 * Constructs a where clause that matches no records.
 *
 * @author Marvin S. Addison
 * @since 1.0
 */
public class NoMatchWhereClauseMatchCriteria extends AbstractWhereClauseMatchCriteria {
    public NoMatchWhereClauseMatchCriteria() {
        sbClause.append("WHERE 0=1");
    }
}
