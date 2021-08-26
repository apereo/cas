package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("HazelcastCoreProperties")
public class HazelcastCoreProperties implements Serializable {
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
}
