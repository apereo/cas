package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * A proxy policy that only allows proxying to pgt urls
 * that match the specified regex pattern.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class RegexMatchingRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    private static final long serialVersionUID = -211069319543047324L;
    
    private final Pattern pattern;

    /**
     * Instantiates a new Regex matching registered service proxy policy.
     * Required for serialization.
     */
    protected RegexMatchingRegisteredServiceProxyPolicy() {
        this.pattern = null;
    }

    /**
     * Init the policy with the pgt url regex pattern that
     * will determine the urls allowed to receive the pgt.
     * The matching by default is done in a case insensitive manner.
     * @param pgtUrlPattern the pgt url pattern
     */
    public RegexMatchingRegisteredServiceProxyPolicy(@NotNull final String pgtUrlPattern) {
        this.pattern = Pattern.compile(pgtUrlPattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     */
    public Pattern getPattern() {
        return this.pattern;
    }

    @Override
    public boolean isAllowedToProxy() {
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 117).append(this.pattern.pattern()).toHashCode();
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
        final RegexMatchingRegisteredServiceProxyPolicy rhs = (RegexMatchingRegisteredServiceProxyPolicy) obj;
        return new EqualsBuilder().append(this.pattern.pattern(), rhs.pattern.pattern()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(this.pattern.pattern()).toString();
    }

    @Override
    public boolean isAllowedProxyCallbackUrl(final URL pgtUrl) {
        return this.pattern.matcher(pgtUrl.toExternalForm()).find();
    }

}
