package org.apereo.cas.configuration.model.support.saml.shibboleth;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link ShibbolethIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-shibboleth")
public class ShibbolethIdPProperties implements Serializable {
    private static final long serialVersionUID = 1741075420882227768L;
    /**
     * The server url of the shibboleth idp deployment.
     */
    private String serverUrl;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
