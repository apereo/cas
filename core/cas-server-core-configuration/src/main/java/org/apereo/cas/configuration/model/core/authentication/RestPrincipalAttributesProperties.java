package org.apereo.cas.configuration.model.core.authentication;

import java.io.Serializable;

/**
 * This is {@link RestPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestPrincipalAttributesProperties implements Serializable {
    private static final long serialVersionUID = -30055974448426360L;
    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * The endpoint URL to contact and retrieve attributes.
     */
    private String url;

    /**
     * HTTP method to use when contacting the rest endpoint.
     * Examples include {@code GET, POST}, etc.
     */
    private String method;

    /**
     * Whether attribute repository should consider the underlying
     * attribute names in a case-insensitive manner.
     */
    private boolean caseInsensitive;

    /**
     * If REST endpoint is protected via basic authentication,
     * specify the username for authentication.
     */
    private String basicAuthUsername;
    /**
     * If REST endpoint is protected via basic authentication,
     * specify the password for authentication.
     */
    private String basicAuthPassword;

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(final boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public void setBasicAuthUsername(final String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public void setBasicAuthPassword(final String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
    }
}
