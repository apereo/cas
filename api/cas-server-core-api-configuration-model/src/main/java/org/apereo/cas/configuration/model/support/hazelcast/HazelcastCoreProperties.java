package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link HazelcastCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5935324429402972680L;

    /**
     * Hazelcast enterprise license key.
     */
    private String licenseKey;

    /**
     * Enables compression when default java serialization is used.
     */
    private boolean enableCompression;

    /**
     * Enables scripting from Management Center.
     */
    private boolean enableManagementCenterScripting = true;

    /**
     * Enable Jet configuration/service on the hazelcast instance.
     * Hazelcast Jet is a distributed batch and stream processing system
     * that can do stateful computations over massive amounts of data with consistent low latency.
     * Jet service is required when executing SQL queries with the SQL service.
     */
    private boolean enableJet = true;
}
