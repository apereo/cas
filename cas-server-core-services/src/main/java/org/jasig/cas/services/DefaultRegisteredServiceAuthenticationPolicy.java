package org.jasig.cas.services;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class DefaultRegisteredServiceAuthenticationPolicy implements RegisteredServiceAuthenticationPolicy {

    private static final long serialVersionUID = -3068390754996358337L;
    private Set<String> multifactorAuthenticationProviders = new LinkedHashSet<>();
    private boolean failOpen;

    /**
     * Instantiates a new Default registered service authentication policy.
     */
    public DefaultRegisteredServiceAuthenticationPolicy() {
    }

    @Override
    public Set<String> getMultifactorAuthenticationProviders() {
        return multifactorAuthenticationProviders;
    }

    public void setMultifactorAuthenticationProviders(final Set<String> multifactorAuthenticationProviders) {
        this.multifactorAuthenticationProviders = multifactorAuthenticationProviders;
    }

    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(final boolean failOpen) {
        this.failOpen = failOpen;
    }
}
