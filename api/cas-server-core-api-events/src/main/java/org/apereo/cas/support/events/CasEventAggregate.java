package org.apereo.cas.support.events;

import org.jspecify.annotations.Nullable;
import java.time.LocalDateTime;

/**
 * This is {@link CasEventAggregate}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record CasEventAggregate(@Nullable LocalDateTime dateTime, @Nullable String type, @Nullable Long count, @Nullable String tenant) {
}
