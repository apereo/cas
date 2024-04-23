package org.apereo.cas.clouddirectory;

import java.util.List;
import java.util.Map;

/**
 * This is {@link AmazonCloudDirectoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface AmazonCloudDirectoryRepository {

    /**
     * Gets user from cloud directory along with all other attributes.
     *
     * @param username the username
     * @return the user account map
     */
    Map<String, List<Object>> getUser(String username);
}
