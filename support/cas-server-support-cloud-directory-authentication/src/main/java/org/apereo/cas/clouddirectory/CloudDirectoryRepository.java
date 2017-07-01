package org.apereo.cas.clouddirectory;

import java.util.Map;

/**
 * This is {@link CloudDirectoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface CloudDirectoryRepository {

    /**
     * Gets user from cloud directory along with all other attributes.
     *
     * @param username the username
     * @return the user account map
     */
    Map<String, Object> getUser(String username);
}
