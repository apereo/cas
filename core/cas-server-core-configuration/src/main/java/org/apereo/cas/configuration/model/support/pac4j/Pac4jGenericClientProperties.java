package org.apereo.cas.configuration.model.support.pac4j;

/**
 * This is {@link Pac4jGenericClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class Pac4jGenericClientProperties {
    /**
     * The client id.
     */
    private String id;
    /**
     * The client secret.
     */
    private String secret;

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }
}
