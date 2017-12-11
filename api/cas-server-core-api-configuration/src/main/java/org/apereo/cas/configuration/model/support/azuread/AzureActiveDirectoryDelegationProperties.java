package org.apereo.cas.configuration.model.support.azuread;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link AzureActiveDirectoryDelegationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-azure-ad-authentication")
public class AzureActiveDirectoryDelegationProperties implements Serializable {
    private static final long serialVersionUID = 5934626199445757568L;

    /**
     * Azure active directory instance url.
     */
    @RequiredProperty
    private String instance = "https://login.microsoftonline.com/";

    /**
     * Azure active directory tenant id.
     */
    @RequiredProperty
    private String tenant;

    /**
     * Azure active directory client id.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Azure active directory extra query parameters to append to the authentication request.
     */
    private String extraQueryParameters = "nux=1";

    /**
     * Azure active directory client secret.
     */
    @RequiredProperty
    private String clientSecret;

    /**
     * Groups and roles to retrieve from Azure.
     */
    private String groups;

    public String getInstance() {
        return StringUtils.appendIfMissing(instance, "/");
    }

    public void setInstance(final String instance) {
        this.instance = instance;
    }

    public String getTenant() {
        return StringUtils.appendIfMissing(tenant, "/");
    }

    public void setTenant(final String tenant) {
        this.tenant = tenant;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getExtraQueryParameters() {
        return extraQueryParameters;
    }

    public void setExtraQueryParameters(final String extraQueryParameters) {
        this.extraQueryParameters = extraQueryParameters;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(final String groups) {
        this.groups = groups;
    }
}
