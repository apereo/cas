package org.apereo.cas.configuration.model.support.hazelcast;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link HazelcastDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
public class HazelcastDiscoveryProperties implements Serializable {
    private static final long serialVersionUID = -8281223487171101795L;

    /**
     * Whether discovery should be enabled via the configured strategies below.
     */
    private boolean enabled;

    /**
     * Describe discovery strategy based on AWS.
     * The AWS config contains the configuration for AWS join mechanism.
     * What happens behind the scenes is that data about the running AWS instances in a specific region are downloaded using the
     * accesskey/secretkey and are potential Hazelcast members.
     * There are 2 mechanisms for filtering out AWS instances and these mechanisms can be combined (AND).
     * <ol>
     * <li>If a security group is configured, only instances within that security group are selected.</li>
     * <li>If a tag key/value is set, only instances with that tag key/value will be selected.</li>
     * </ol>
     * Once Hazelcast has figured out which instances are available, it will use the private IP addresses of these
     * instances to create a TCP/IP-cluster.
     */
    private HazelcastAwsDiscoveryProperties aws = new HazelcastAwsDiscoveryProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public HazelcastAwsDiscoveryProperties getAws() {
        return aws;
    }

    public void setAws(final HazelcastAwsDiscoveryProperties aws) {
        this.aws = aws;
    }

    public static class HazelcastAwsDiscoveryProperties {

        /**
         * AWS access key.
         */
        @RequiredProperty
        private String accessKey;

        /**
         * AWS secret key.
         */
        @RequiredProperty
        private String secretKey;
        /**
         * If you do not want to use access key and secret key, you can specify iam-role.
         * Hazelcast fetches your credentials by using your IAM role.
         */
        private String iamRole;
        /**
         * AWS region. i.e. {@code us-east-1}.
         * The region where your members are running.
         */
        private String region = "us-east-1";
        /**
         * Host header. i.e. {@code ec2.amazonaws.com}.
         * The URL that is the entry point for a web service.
         */
        private String hostHeader;
        /**
         * If a security group is configured, only instances within that security group are selected.
         */
        private String securityGroupName;
        /**
         * If a tag key/value is set, only instances with that tag key/value will be selected.
         */
        private String tagKey;
        /**
         * If a tag key/value is set, only instances with that tag key/value will be selected.
         */
        private String tagValue;
        /**
         * Hazelcast port. Typically may be set to {@code 5701}.
         * You can set searching for other ports rather than 5701 if you've members on different ports
         */
        private int port = -1;

        /**
         * The maximum amount of time Hazelcast will try to connect to a well known
         * member before giving up. Setting this value too low could mean that a
         * member is not able to connect to a cluster. Setting the value too high means
         * that member startup could slow down because of longer timeouts (for example,
         * when a well known member is not up). Increasing this value is recommended if
         * you have many IPs listed and the members cannot properly build up the cluster.
         * Its default value is 5.
         */
        private int connectionTimeoutSeconds = 5;

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(final String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(final String secretKey) {
            this.secretKey = secretKey;
        }

        public String getIamRole() {
            return iamRole;
        }

        public void setIamRole(final String iamRole) {
            this.iamRole = iamRole;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(final String region) {
            this.region = region;
        }

        public String getHostHeader() {
            return hostHeader;
        }

        public void setHostHeader(final String hostHeader) {
            this.hostHeader = hostHeader;
        }

        public String getSecurityGroupName() {
            return securityGroupName;
        }

        public void setSecurityGroupName(final String securityGroupName) {
            this.securityGroupName = securityGroupName;
        }

        public String getTagKey() {
            return tagKey;
        }

        public void setTagKey(final String tagKey) {
            this.tagKey = tagKey;
        }

        public String getTagValue() {
            return tagValue;
        }

        public void setTagValue(final String tagValue) {
            this.tagValue = tagValue;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }
    }
}
