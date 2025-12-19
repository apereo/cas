package org.apereo.cas.util.text;

import module java.base;

/**
 * This is {@link MessageSanitationContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface MessageSanitationContributor {
    /**
     * Gets ticket identifier prefixes.
     *
     * @return the ticket identifier prefixes
     */
    List<String> getTicketIdentifierPrefixes();
}
