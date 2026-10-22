package org.apereo.cas.configuration.model.support.hazelcast;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
     * This applies to values Hazelcast stores with Java serialization, such as authentication throttling
     * records and streamed service definitions. Tickets of the Hazelcast ticket registry are stored with
     * compact serialization and are not compressed.
     */
    private boolean enableCompression;

    /**
     * Enables scripting from Management Center.
     */
    private boolean enableManagementCenterScripting = true;
}
