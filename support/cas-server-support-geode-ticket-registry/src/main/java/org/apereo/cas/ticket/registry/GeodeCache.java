package org.apereo.cas.ticket.registry;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Region;

/**
 * This is {@link GeodeCache}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public record GeodeCache(Cache cache, Region<String, GeodeTicketDocument> region) {
}
