package org.apereo.inspektr.audit;

import module java.base;

/**
 * Asks an audit storage how many records it holds per action and per hour, between two times, for a set of actions.
 * Times are the times the records carry, cut to the hour.
 *
 * @param from    the first hour asked for
 * @param to      the end of the period, not included
 * @param actions the actions to count
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public record AuditSummaryQuery(LocalDateTime from, LocalDateTime to, Set<String> actions) {
    /**
     * The length of one summary bucket.
     */
    public static final Duration BUCKET = Duration.ofHours(1);

    public AuditSummaryQuery {
        from = from.truncatedTo(ChronoUnit.HOURS);
        actions = Set.copyOf(actions);
    }

    /**
     * The hours the period covers, first to last.
     *
     * @return the start of each hour
     */
    public List<LocalDateTime> hours() {
        return Stream.iterate(from, hour -> hour.isBefore(to), hour -> hour.plus(BUCKET)).toList();
    }

    /**
     * The hour a time falls in.
     *
     * @param time the time
     * @return the start of its hour
     */
    public static LocalDateTime hourOf(final LocalDateTime time) {
        return time.truncatedTo(ChronoUnit.HOURS);
    }
}
