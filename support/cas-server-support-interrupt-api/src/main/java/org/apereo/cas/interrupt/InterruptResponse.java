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
    private boolean block;
    private boolean ssoEnabled;
    private boolean interrupt;
    private boolean autoRedirect;
    private long autoRedirectAfterSeconds = -1;

    public InterruptResponse() {
        this.interrupt = false;
    }

    public InterruptResponse(final String message, final boolean block,
                             final boolean ssoEnabled) {
        this.message = message;
        this.block = block;
        this.ssoEnabled = ssoEnabled;
        this.interrupt = true;
    }
    
    public InterruptResponse(final boolean interrupt) {
        this.interrupt = interrupt;
    }

    public InterruptResponse(final String message, 
                             final Map<String, String> links,
                             final boolean block,
                             final boolean ssoEnabled) {
        this.message = message;
        this.links = links;
        this.block = block;
        this.ssoEnabled = ssoEnabled;
        this.interrupt = true;
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(final boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    public long getAutoRedirectAfterSeconds() {
        return autoRedirectAfterSeconds;
    }

    public void setAutoRedirectAfterSeconds(final long autoRedirectAfterSeconds) {
        this.autoRedirectAfterSeconds = autoRedirectAfterSeconds;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(final boolean interrupt) {
        this.interrupt = interrupt;
    }

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

    public boolean isBlock() {
        return block;
    }

    public void setBlock(final boolean block) {
        this.block = block;
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
                .append("block", block)
                .append("ssoEnabled", ssoEnabled)
                .append("interrupt", interrupt)
                .append("autoRedirect", autoRedirect)
                .append("autoRedirectAfterSeconds", autoRedirectAfterSeconds)
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
                .append(this.block, rhs.block)
                .append(this.ssoEnabled, rhs.ssoEnabled)
                .append(this.interrupt, rhs.interrupt)
                .append(this.autoRedirect, rhs.autoRedirect)
                .append(this.autoRedirectAfterSeconds, rhs.autoRedirectAfterSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(message)
                .append(links)
                .append(block)
                .append(ssoEnabled)
                .append(interrupt)
                .append(autoRedirect)
                .append(autoRedirectAfterSeconds)
                .toHashCode();
    }
}
