package org.apereo.cas.ha;

import module java.base;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link ClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface ClusterTopologyManager extends NamedObject {
    /**
     * Discover members list.
     *
     * @return the list
     * @throws Exception the exception
     */
    List<? extends ClusterMember> discoverMembers() throws Exception;
}
