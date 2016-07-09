package org.apereo.cas.configuration.model.support.rest;

/**
 * This is {@link RestAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestAuthenticationProperties {
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }
}
