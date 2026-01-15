package org.apereo.inspektr.audit.support;

import module java.base;
import lombok.val;

/**
 * Produces a where clause to select audit records older than a given duration.
 *
 * @author Misagh Moayyed
 * @since 2.0
 */
public class DurationWhereClauseMatchCriteria extends AbstractWhereClauseMatchCriteria {

    private static final String DATE_COLUMN = "AUD_DATE";

    protected final String duration;

    public DurationWhereClauseMatchCriteria(final String duration) {
        this.duration = duration;
        addCriteria(DATE_COLUMN, "<");
    }

    @Override
    public List<?> getParameterValues() {
        val newTime = LocalDateTime.now(Clock.systemUTC()).toEpochSecond(ZoneOffset.UTC)
            + Duration.parse(duration).toMillis();
        return List.of(newTime);
    }
}
