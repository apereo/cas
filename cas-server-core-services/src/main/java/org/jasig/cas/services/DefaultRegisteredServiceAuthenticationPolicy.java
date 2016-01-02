package org.jasig.cas.services;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class DefaultRegisteredServiceAuthenticationPolicy implements RegisteredServiceAuthenticationPolicy {

    private String authenticationMethod;

    /**
     * Instantiates a new Default registered service authentication policy.
     */
    public DefaultRegisteredServiceAuthenticationPolicy() {
    }

    @Override
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(final String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
