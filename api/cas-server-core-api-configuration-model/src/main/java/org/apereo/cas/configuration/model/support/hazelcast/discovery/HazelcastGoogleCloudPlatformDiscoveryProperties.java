package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link HazelcastGoogleCloudPlatformDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-discovery-gcp")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastGoogleCloudPlatformDiscoveryProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6056456067944569289L;

    /**
     * A filesystem path to the private key for GCP service account in the
     * JSON format; if not set, the access token is fetched from the GCP VM instance.
     */
    private String privateKeyPath;

    /**
     * A list of projects where the plugin looks for instances; if not set, the current project is used.
     */
    private String projects;

    /**
     * A list of zones where the plugin looks for instances; if not set, all zones of the current region are used.
     */
    private String zones;

    /**
     * A filter to look only for instances labeled as specified; property format: {@code key=value}.
     */
    private String label;

    /**
     * A range of ports where the plugin looks for Hazelcast members.
     */
    private String hzPort = "5701-5708";

    /**
     * A region where the plugin looks for instances; if not set,
     * the {@link #getZones()} property is used; if it and {@link #getZones()} property not set,
     * all zones of the current region are used.
     */
    private String region;
}
