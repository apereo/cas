package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class DefaultRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {

    private static final long serialVersionUID = -3068390754996358337L;

    private Set<String> multifactorAuthenticationProviders = new LinkedHashSet<>();
    private FailureModes failureMode = FailureModes.CLOSED;
    private String principalAttributeNameTrigger;
    private String principalAttributeValueToMatch;

    /**
     * Instantiates a new Default registered service authentication policy.
     */
    public DefaultRegisteredServiceMultifactorPolicy() {
    }

    @Override
    public Set<String> getMultifactorAuthenticationProviders() {
        return multifactorAuthenticationProviders;
    }

    public void setMultifactorAuthenticationProviders(final Set<String> multifactorAuthenticationProviders) {
        this.multifactorAuthenticationProviders = multifactorAuthenticationProviders;
    }

    @Override
    public FailureModes getFailureMode() {
        return failureMode;
    }

    public void setFailureMode(final FailureModes failureMode) {
        this.failureMode = failureMode;
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


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final DefaultRegisteredServiceMultifactorPolicy rhs = (DefaultRegisteredServiceMultifactorPolicy) obj;
        return new EqualsBuilder()
                .append(this.multifactorAuthenticationProviders, rhs.multifactorAuthenticationProviders)
                .append(this.failureMode, rhs.failureMode)
                .append(this.principalAttributeNameTrigger, rhs.principalAttributeNameTrigger)
                .append(this.principalAttributeValueToMatch, rhs.principalAttributeValueToMatch)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(multifactorAuthenticationProviders)
                .append(failureMode)
                .append(principalAttributeNameTrigger)
                .append(principalAttributeValueToMatch)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("multifactorAuthenticationProviders", multifactorAuthenticationProviders)
                .append("failureMode", failureMode)
                .append("principalAttributeNameTrigger", principalAttributeNameTrigger)
                .append("principalAttributeValueToMatch", principalAttributeValueToMatch)
                .toString();
    }
}
