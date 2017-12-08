package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link BaseHazelcastProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
public class BaseHazelcastProperties implements Serializable {
    /**
     * Logging type property name.
     */
    public static final String LOGGING_TYPE_PROP = "hazelcast.logging.type";
    /**
     * Enable discovery.
     */
    public static final String HAZELCAST_DISCOVERY_ENABLED = "hazelcast.discovery.enabled";

    /**
     * AWS discovery access key property.
     */
    public static final String AWS_DISCOVERY_ACCESS_KEY = "access-key";
    /**
     * AWS discovery secret key property.
     */
    public static final String AWS_DISCOVERY_SECRET_KEY = "secret-key";
    /**
     * AWS discovery IAM role property.
     */
    public static final String AWS_DISCOVERY_IAM_ROLE = "iam-role";
    /**
     * AWS discovery  region property.
     */
    public static final String AWS_DISCOVERY_REGION = "region";
    /**
     * AWS discovery host header property.
     */
    public static final String AWS_DISCOVERY_HOST_HEADER = "host-header";
    /**
     * AWS discovery security group name property.
     */
    public static final String AWS_DISCOVERY_SECURITY_GROUP_NAME = "security-group-name";
    /**
     * AWS discovery  tag key property.
     */
    public static final String AWS_DISCOVERY_TAG_KEY = "tag-key";
    /**
     * AWS discovery tag value property.
     */
    public static final String AWS_DISCOVERY_TAG_VALUE = "tag-value";
    /**
     * AWS discovery HZ port property.
     */
    public static final String AWS_DISCOVERY_PORT = "hz-port";
    /**
     * Max num of seconds for heartbeat property name.
     */
    public static final String MAX_HEARTBEAT_SECONDS_PROP = "hazelcast.max.no.heartbeat.seconds";
    /**
     * Ipv4 protocol stack.
     */
    public static final String IPV4_STACK_PROP = "hazelcast.prefer.ipv4.stack";

    private static final long serialVersionUID = 4204884717547468480L;

    /**
     * Hazelcast cluster settings if CAS is able to auto-create caches.
     */
    @NestedConfigurationProperty
    private HazelcastClusterProperties cluster = new HazelcastClusterProperties();

    public HazelcastClusterProperties getCluster() {
        return cluster;
    }

    public void setCluster(final HazelcastClusterProperties cluster) {
        this.cluster = cluster;
    }
}
