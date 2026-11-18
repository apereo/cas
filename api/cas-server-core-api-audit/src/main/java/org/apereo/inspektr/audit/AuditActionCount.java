package org.apereo.inspektr.audit;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * How many audit records of one action were made in one hour, and for one target when counted by target.
 *
 * @author Misagh Moayyed
 * @param hour   the start of the hour
 * @param action the action
 * @param count  the number of records
 * @param target what the records were for, when counted by target
 * @since 8.1.0
 */
public record AuditActionCount(LocalDateTime hour, String action, long count, @Nullable String target) {
    public AuditActionCount(final LocalDateTime hour, final String action, final long count) {
        this(hour, action, count, null);
    }
}
