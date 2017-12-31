package org.apereo.cas.configuration.model.support.syncope;

import java.io.Serializable;

/**
 * This is {@link SyncopeAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SyncopeAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -2446926316502297496L;

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Syncope domain used for authentication, etc.
     */
    private String domain = "Master";
    
    /**
     * Syncope instance URL primary used for REST.
     */
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }
}
