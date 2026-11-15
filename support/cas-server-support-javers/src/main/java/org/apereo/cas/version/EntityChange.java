package org.apereo.cas.version;

import module java.base;

/**
 * One change to an entity in its history: which entity, what happened to it, who did it and when. The action is
 * {@code created}, {@code updated} or {@code deleted}; the name is the entity's name as it was last recorded.
 *
 * @author Misagh Moayyed
 * @param id     the entity's identifier
 * @param type   the entity's type
 * @param name   the entity's name, if it has one
 * @param action what happened to the entity
 * @param author who made the change
 * @param when   when the change was made
 * @since 8.1.0
 */
public record EntityChange(String id, String type, String name, String action, String author, Instant when) implements Serializable {
    @Serial
    private static final long serialVersionUID = 3591746211730592447L;
}
