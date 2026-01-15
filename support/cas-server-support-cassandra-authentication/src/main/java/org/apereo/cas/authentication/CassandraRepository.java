package org.apereo.cas.authentication;

import module java.base;

/**
 * This is {@link CassandraRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface CassandraRepository {

    /**
     * Gets user.
     *
     * @param uid the uid
     * @return the user
     */
    Map<String, List<Object>> getUser(String uid);
}
