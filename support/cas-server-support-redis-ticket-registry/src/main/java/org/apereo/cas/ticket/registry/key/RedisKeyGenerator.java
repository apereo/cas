package org.apereo.cas.ticket.registry.key;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link RedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface RedisKeyGenerator {

    /**
     * Gets type.
     *
     * @return the type
     */
    String getType();

    /**
     * For all entries string.
     *
     * @return the string
     */
    String forAllEntries();

    /**
     * For entry string.
     *
     * @param entry the entry
     * @return the string
     */
    default String forEntry(final String entry) {
        return forEntry(StringUtils.EMPTY, entry);
    }

    /**
     * For entry string.
     *
     * @param type  the type
     * @param entry the entry
     * @return the string
     */
    String forEntry(String type, String entry);

    /**
     * For entry type string.
     *
     * @param type the type
     * @return the string
     */
    default String forEntryType(final String type) {
        return forAllEntries();
    }

    /**
     * Raw key string.
     *
     * @param type the type
     * @return the string
     */
    default String rawKey(final String type) {
        return type;
    }

    /**
     * Gets namespace.
     *
     * @return the namespace
     */
    String getNamespace();
    
}
