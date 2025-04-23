package org.apereo.cas.support.events;

import java.time.LocalDateTime;

/**
 * This is {@link CasEventAggregate}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record CasEventAggregate(LocalDateTime dateTime, String type, Long count, String tenant) {
}
