package org.apereo.cas.authentication;

import java.util.Map;

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
    Map<String, Object> getUser(String uid);
}
