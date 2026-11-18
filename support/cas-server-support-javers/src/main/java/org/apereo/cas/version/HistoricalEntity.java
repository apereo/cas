package org.apereo.cas.version;

import module java.base;

/**
 * A version of an entity from its history: the entity as it was, the id of the change that recorded it, when that
 * change was made and who made it. The author is the name of the user who was signed in when the change was made,
 * or {@value JaversEntityHistoryRepository#SYSTEM_AUTHOR} for changes CAS made on its own.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public record HistoricalEntity(Serializable entity, String id, LocalDateTime date, String author) implements Serializable {
    @Serial
    private static final long serialVersionUID = -7019698110076853057L;
}
