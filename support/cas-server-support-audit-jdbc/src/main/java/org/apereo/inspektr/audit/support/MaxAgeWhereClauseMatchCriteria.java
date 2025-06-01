package org.apereo.inspektr.audit.support;

import lombok.val;
import java.util.Calendar;
import java.util.List;

/**
 * Produces a where clause to select audit records older than some arbitrary
 * cutoff age in days.
 *
 * @author Marvin S. Addison
 * @since 1.0
 *
 */
public class MaxAgeWhereClauseMatchCriteria extends AbstractWhereClauseMatchCriteria {

    /** Name of creation date column name in audit record table. */
    private static final String DATE_COLUMN = JdbcAuditTrailManager.AuditTableColumns.DATE.getColumnName();

    protected final int maxAge;


    /**
     * Creates a new instance that selects audit records older than the given
     * number of days as measured from the present time.
     *
     * @param maxAgeDays Cutoff age of records in days.
     */
    public MaxAgeWhereClauseMatchCriteria(final int maxAgeDays) {
        this.maxAge = maxAgeDays;
        addCriteria(DATE_COLUMN, "<");
    }


    @Override
    public List<?> getParameterValues() {
        val cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -this.maxAge);
        return List.of(cal.getTime());
    }
}
