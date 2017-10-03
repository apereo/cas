package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultAssertionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultAssertionBuilder {
    /**
     * The Auth.
     */
    private final Authentication auth;
    /**
     * The Authentications.
     */
    private List<Authentication> authentications = new ArrayList<>();
    /**
     * The Service.
     */
    private Service service;
    /**
     * The New login.
     */
    private boolean newLogin;

    /**
     * Instantiates a new Default assertion builder.
     *
     * @param auth the auth
     */
    public DefaultAssertionBuilder(final Authentication auth) {
        this.auth = auth;
    }

    /**
     * With default assertion builder.
     *
     * @param authentications the authentications
     * @return the default assertion builder
     */
    public DefaultAssertionBuilder with(final List<Authentication> authentications) {
        this.authentications = authentications;
        return this;
    }

    /**
     * With default assertion builder.
     *
     * @param service the service
     * @return the default assertion builder
     */
    public DefaultAssertionBuilder with(final Service service) {
        this.service = service;
        return this;
    }

    /**
     * With default assertion builder.
     *
     * @param newLogin the new login
     * @return the default assertion builder
     */
    public DefaultAssertionBuilder with(final boolean newLogin) {
        this.newLogin = newLogin;
        return this;
    }

    /**
     * Build assertion.
     *
     * @return the assertion
     */
    public Assertion build() {
        return new ImmutableAssertion(this.auth, this.authentications, this.service, this.newLogin);
    }
}
