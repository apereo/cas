package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {

    private static final long serialVersionUID = -3068390754996358337L;

    private Set<String> multifactorAuthenticationProviders = new LinkedHashSet<>();

    private FailureModes failureMode = FailureModes.NOT_SET;

    private String principalAttributeNameTrigger;

    private String principalAttributeValueToMatch;

    private boolean bypassEnabled;

    /**
     * Instantiates a new Default registered service authentication policy.
     */
    public DefaultRegisteredServiceMultifactorPolicy() {
    }

    @Override
    public Set<String> getMultifactorAuthenticationProviders() {
        return this.multifactorAuthenticationProviders;
    }

    public void setMultifactorAuthenticationProviders(final Set<String> multifactorAuthenticationProviders) {
        this.multifactorAuthenticationProviders = multifactorAuthenticationProviders;
    }

    @Override
    public FailureModes getFailureMode() {
        return this.failureMode;
    }

    public void setFailureMode(final FailureModes failureMode) {
        this.failureMode = failureMode;
    }

    @Override
    public String getPrincipalAttributeNameTrigger() {
        return this.principalAttributeNameTrigger;
    }

    public void setPrincipalAttributeNameTrigger(final String principalAttributeNameTrigger) {
        this.principalAttributeNameTrigger = principalAttributeNameTrigger;
    }

    @Override
    public String getPrincipalAttributeValueToMatch() {
        return this.principalAttributeValueToMatch;
    }

    public void setPrincipalAttributeValueToMatch(final String principalAttributeValueToMatch) {
        this.principalAttributeValueToMatch = principalAttributeValueToMatch;
    }

    public void setBypassEnabled(final boolean bypass) {
        this.bypassEnabled = bypass;
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
                .append(this.bypassEnabled, rhs.bypassEnabled)
                .isEquals();
    }

    @Override
    public boolean isBypassEnabled() {
        return this.bypassEnabled;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.multifactorAuthenticationProviders)
                .append(this.failureMode)
                .append(this.principalAttributeNameTrigger)
                .append(this.principalAttributeValueToMatch)
                .append(this.bypassEnabled)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("multifactorAuthenticationProviders", this.multifactorAuthenticationProviders)
                .append("failureMode", this.failureMode)
                .append("principalAttributeNameTrigger", this.principalAttributeNameTrigger)
                .append("principalAttributeValueToMatch", this.principalAttributeValueToMatch)
                .append("bypassEnabled", this.bypassEnabled)
                .toString();
    }
}
