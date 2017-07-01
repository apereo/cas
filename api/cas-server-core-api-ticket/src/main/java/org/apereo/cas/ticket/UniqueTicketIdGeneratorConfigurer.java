package org.apereo.cas.ticket;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

/**
 * This is {@link UniqueTicketIdGeneratorConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface UniqueTicketIdGeneratorConfigurer {

    /**
     * Build unique ticket id generators collection.
     *
     * @return the collection
     */
    Collection<Pair<String, UniqueTicketIdGenerator>> buildUniqueTicketIdGenerators();
}
