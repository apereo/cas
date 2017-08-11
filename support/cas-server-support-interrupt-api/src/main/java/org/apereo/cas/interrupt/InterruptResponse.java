package org.apereo.cas.interrupt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link InterruptResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptResponse implements Serializable {
    private static final long serialVersionUID = 2558836528840508196L;
    
    private String message;
    private Map<String, String> links = new LinkedHashMap<>();
    private boolean enabled;
    private boolean ssoEnabled;

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(final Map<String, String> links) {
        this.links = links;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .append("links", links)
                .append("enabled", enabled)
                .append("ssoEnabled", ssoEnabled)
                .toString();
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
        final InterruptResponse rhs = (InterruptResponse) obj;
        return new EqualsBuilder()
                .append(this.message, rhs.message)
                .append(this.links, rhs.links)
                .append(this.enabled, rhs.enabled)
                .append(this.ssoEnabled, rhs.ssoEnabled)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(message)
                .append(links)
                .append(enabled)
                .append(ssoEnabled)
                .toHashCode();
    }
}
