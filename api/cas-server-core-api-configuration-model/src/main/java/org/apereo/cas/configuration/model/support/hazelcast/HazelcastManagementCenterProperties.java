package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link HazelcastManagementCenterProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
public class HazelcastManagementCenterProperties implements Serializable {
    private static final long serialVersionUID = 2215584617045775145L;

    /**
     * {@code true} if management center is enabled, {@code false} if disabled.
     */
    private boolean enabled;

    /**
     * Gets the URL where management center will work.
     */
    private String url;

    /**
     * Gets the time frequency (in seconds) for which Management Center will take
     * information from the Hazelcast cluster.
     */
    private int updateInterval = 5;
}
