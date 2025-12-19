package org.apereo.cas.support.events;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link CasEventAggregate}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record CasEventAggregate(@Nullable LocalDateTime dateTime, @Nullable String type, @Nullable Long count, @Nullable String tenant) {
}
