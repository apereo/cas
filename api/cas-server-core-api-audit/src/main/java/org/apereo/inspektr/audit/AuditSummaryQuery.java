package org.apereo.inspektr.audit;

import module java.base;

/**
 * Asks an audit storage how many records it holds per action and per hour, between two times, for a set of actions,
 * and when asked, per target too: what each action was for, such as an application or a multifactor provider; records
 * without a target are then left out. Times are the times the records carry, cut to the hour.
 *
 * @author Misagh Moayyed
 * @param from     the first hour asked for
 * @param to       the end of the period, not included
 * @param actions  the actions to count
 * @param byTarget whether to count per target as well
 * @since 8.1.0
 */
public record AuditSummaryQuery(LocalDateTime from, LocalDateTime to, Set<String> actions, boolean byTarget) {
    /**
     * The length of one summary bucket.
     */
    public static final Duration BUCKET = Duration.ofHours(1);

    public AuditSummaryQuery {
        from = from.truncatedTo(ChronoUnit.HOURS);
        actions = Set.copyOf(actions);
    }

    public AuditSummaryQuery(final LocalDateTime from, final LocalDateTime to, final Set<String> actions) {
        this(from, to, actions, false);
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
