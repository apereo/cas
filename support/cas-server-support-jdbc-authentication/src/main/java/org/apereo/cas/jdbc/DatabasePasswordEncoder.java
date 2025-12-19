package org.apereo.cas.jdbc;

import module java.base;

/**
 * This is {@link DatabasePasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface DatabasePasswordEncoder {
    /**
     * Encode the given password, give the results of the SQL query.
     * The provided password is often supplied by the user, and is then encoded
     * and digested using the query results here (salt, iterations, etc) that attached
     * to that record and password. The final result, that is the encoded password, can then
     * be compared with the actual encoded password found for the user record.
     *
     * @param password    the password
     * @param queryValues the query values
     * @return the object
     */
    String encode(String password, Map<String, Object> queryValues);
}
