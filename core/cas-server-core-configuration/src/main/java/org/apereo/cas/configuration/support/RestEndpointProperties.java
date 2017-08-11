package org.apereo.cas.configuration.support;

import java.io.Serializable;

/**
 * This is {@link RestEndpointProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestEndpointProperties implements Serializable {
    private static final long serialVersionUID = 2687020856160473089L;

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
     * If REST endpoint is protected via basic authentication,
     * specify the username for authentication.
     */
    private String basicAuthUsername;
    /**
     * If REST endpoint is protected via basic authentication,
     * specify the password for authentication.
     */
    private String basicAuthPassword;

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
