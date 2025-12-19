package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link HazelcastAwsDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-discovery-aws")
@Getter
@Setter
@Accessors(chain = true)
public class HazelcastAwsDiscoveryProperties implements Serializable {

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
     * Property name for connection and read timeouts when making a call to AWS API.
     */
    public static final String AWS_DISCOVERY_CONNECTION_TIMEOUT = "connection-timeout-seconds";

    /**
     * Property name for connection and read timeouts when making a call to AWS API.
     */
    public static final String AWS_DISCOVERY_READ_TIMEOUT = "read-timeout-seconds";

    /**
     * Property name for ECS cluster short name or ARN; default is the current cluster.
     */
    public static final String AWS_DISCOVERY_CLUSTER = "cluster";

    /**
     * Property name for filter to look only for ECS tasks from the
     * given service; mutually exclusive with {@link #AWS_DISCOVERY_FAMILY}.
     */
    public static final String AWS_DISCOVERY_SERVCE_NAME = "service-name";
    
    /**
     * Property name for filter to look only for ECS tasks with the given family name;
     * mutually exclusive with {@link #AWS_DISCOVERY_SERVCE_NAME}.
     */
    public static final String AWS_DISCOVERY_FAMILY = "family";

    @Serial
    private static final long serialVersionUID = -8281247687171101766L;

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
     * Hazelcast fetches your credentials by using your IAM role. This setting only affects
     * deployments on Amazon EC2. If you are deploying CAS in an Amazon ECS environment,
     * the role should not be specified. The role is fetched from the task definition
     * that is assigned to run CAS.
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
     * You can set searching for other ports rather than 5701 if you've members on different ports.
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

    /**
     * ECS cluster short name or ARN; default is the current cluster.
     */
    private String cluster;

    /**
     * Filter to look only for ECS tasks from the
     * given service; mutually exclusive with {@link #getFamily()}.
     */
    private String serviceName;

    /**
     * Filter to look only for ECS tasks with the given family name;
     * mutually exclusive with {@link #getServiceName()}.
     */
    private String family;
}
