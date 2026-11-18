package org.apereo.cas.palantir.overview;

import module java.base;

/**
 * The latest changes to applications from a history that every CAS server shares, such as the service change history.
 * Without one, Palantir lists the changes the server it runs on has seen since it started.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface PalantirApplicationChanges {
    /**
     * The latest changes, newest first.
     *
     * @param limit how many changes to return at most
     * @return the changes, or nothing when no shared history is available right now
     */
    Optional<List<Change>> recent(int limit);

    /**
     * One change to an application.
     *
     * @param id     the application's numeric identifier
     * @param name   the application's name
     * @param type   the application's type, as its definition names it
     * @param action {@code created}, {@code updated}, {@code saved} or {@code deleted}
     * @param author who made the change
     * @param when   when the change was made
     */
    record Change(String id, String name, String type, String action, String author, Instant when) {
    }
}
