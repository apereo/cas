package org.apereo.cas.configuration.model.support.hazelcast.discovery;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link HazelcastAzureDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-hazelcast-core")
public class HazelcastAzureDiscoveryProperties implements Serializable {

    /**
     * Microsoft Azure's client id property for discovery.
     */
    public static final String AZURE_DISCOVERY_CLIENT_ID = "client-id";

    /**
     * Microsoft Azure's client secret property for discovery.
     */
    public static final String AZURE_DISCOVERY_CLIENT_SECRET = "client-secret";

    /**
     * Microsoft Azure's tenant id property for discovery.
     */
    public static final String AZURE_DISCOVERY_TENANT_ID = "tenant-id";

    /**
     * Microsoft Azure's subscription id property for discovery.
     */
    public static final String AZURE_DISCOVERY_SUBSCRIPTION_ID = "subscription-id";

    /**
     * Microsoft Azure's subscription id property for discovery.
     */
    public static final String AZURE_DISCOVERY_CLUSTER_ID = "cluster-id";

    /**
     * Microsoft Azure's group name property for discovery.
     */
    public static final String AZURE_DISCOVERY_GROUP_NAME = "group-name";

    private static final long serialVersionUID = 3861923784551442190L;

    /**
     * The Azure subscription ID.
     */
    @RequiredProperty
    private String subscriptionId;

    /**
     * The Azure Active Directory Service Principal client ID.
     */
    @RequiredProperty
    private String clientId;

    /**
     * The Azure Active Directory Service Principal client secret.
     */
    @RequiredProperty
    private String clientSecret;

    /**
     * The Azure Active Directory tenant ID.
     */
    @RequiredProperty
    private String tenantId;

    /**
     * The name of the tag on the hazelcast vm resources.
     * With every Hazelcast Virtual Machine you deploy in your resource group, you need to ensure that each VM is tagged with the value of cluster-id defined in
     * your Hazelcast configuration. The only requirement is that every VM can access each other either by private or public IP address.
     */
    @RequiredProperty
    private String clusterId;

    /**
     * The Azure resource group name of the cluster. You can find this in the Azure portal or CLI.
     */
    @RequiredProperty
    private String groupName;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(final String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(final String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }
}
