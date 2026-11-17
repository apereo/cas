package org.apereo.inspektr.audit;

import module java.base;

/**
 * How many audit records of one action were made in one hour.
 *
 * @param hour   the start of the hour
 * @param action the action
 * @param count  the number of records
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public record AuditActionCount(LocalDateTime hour, String action, long count) {
}
