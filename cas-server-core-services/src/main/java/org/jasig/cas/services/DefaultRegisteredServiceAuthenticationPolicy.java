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
    private String principalAttributeNameTrigger;
    private String principalAttributeValueToMatch;

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

    @Override
    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(final boolean failOpen) {
        this.failOpen = failOpen;
    }

    @Override
    public String getPrincipalAttributeNameTrigger() {
        return principalAttributeNameTrigger;
    }

    public void setPrincipalAttributeNameTrigger(final String principalAttributeNameTrigger) {
        this.principalAttributeNameTrigger = principalAttributeNameTrigger;
    }

    @Override
    public String getPrincipalAttributeValueToMatch() {
        return principalAttributeValueToMatch;
    }

    public void setPrincipalAttributeValueToMatch(final String principalAttributeValueToMatch) {
        this.principalAttributeValueToMatch = principalAttributeValueToMatch;
    }
}
