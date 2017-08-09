package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * A proxy policy that only allows proxying to pgt urls
 * that match the specified regex pattern.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegexMatchingRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexMatchingRegisteredServiceProxyPolicy.class);
    
    private static final long serialVersionUID = -211069319543047324L;
    
    private String pattern;

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
    @JsonCreator
    public RegexMatchingRegisteredServiceProxyPolicy(@JsonProperty("pattern") final String pgtUrlPattern) {
        if (RegexUtils.isValidRegex(pgtUrlPattern)) {
            this.pattern = pgtUrlPattern;
        } else {
            LOGGER.warn("Pattern specified [{}] is not a valid regular expression", pgtUrlPattern);
            this.pattern = RegexUtils.MATCH_NOTHING_PATTERN.pattern();
        }
    }

    public String getPattern() {
        return this.pattern;
    }

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 117).append(this.pattern).toHashCode();
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
        return new EqualsBuilder().append(this.pattern, rhs.pattern).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(this.pattern).toString();
    }

    @Override
    public boolean isAllowedProxyCallbackUrl(final URL pgtUrl) {
        return RegexUtils.find(this.pattern, pgtUrl.toExternalForm());
    }
}
