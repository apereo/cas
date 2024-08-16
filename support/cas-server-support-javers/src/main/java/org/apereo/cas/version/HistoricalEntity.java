package org.apereo.cas.version;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This is {@link HistoricalEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public record HistoricalEntity(Serializable entity, String id, LocalDateTime date) implements Serializable {
    @Serial
    private static final long serialVersionUID = -7019698110076853056L;
}
