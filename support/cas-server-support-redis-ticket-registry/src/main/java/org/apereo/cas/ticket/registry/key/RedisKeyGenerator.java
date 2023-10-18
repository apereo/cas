package org.apereo.cas.ticket.registry.key;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link RedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RedisKeyGenerator {

    String getType();

    String forAllEntries();

    default String forEntry(final String entry) {
        return forEntry(StringUtils.EMPTY, entry);
    }

    String forEntry(String type, String entry);

    default String forEntryType(final String type) {
        return forAllEntries();
    }

    default String rawKey(final String type) {
        return type;
    }

    String getNamespace();
    
}
