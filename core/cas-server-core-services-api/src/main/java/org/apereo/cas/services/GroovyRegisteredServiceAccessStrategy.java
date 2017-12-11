package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.persistence.Transient;
import java.net.URI;
import java.util.Map;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovyRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {
    private static final long serialVersionUID = -2407494148882123062L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyRegisteredServiceAccessStrategy.class);

    /**
     * The sorting/execution order of this strategy.
     */
    private int order;

    private String groovyScript;

    @JsonIgnore
    @Transient
    private transient RegisteredServiceAccessStrategy groovyStrategyInstance;

    public GroovyRegisteredServiceAccessStrategy() {
    }

    @Override
    @JsonIgnore
    public boolean isServiceAccessAllowed() {
        buildGroovyAccessStrategyInstanceIfNeeded();
        return this.groovyStrategyInstance.isServiceAccessAllowed();
    }

    @Override
    @JsonIgnore
    public boolean isServiceAccessAllowedForSso() {
        buildGroovyAccessStrategyInstanceIfNeeded();
        return this.groovyStrategyInstance.isServiceAccessAllowedForSso();
    }

    @Override
    @JsonIgnore
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> attributes) {
        buildGroovyAccessStrategyInstanceIfNeeded();
        return this.groovyStrategyInstance.doPrincipalAttributesAllowServiceAccess(principal, attributes);
    }

    @JsonIgnore
    @Override
    public URI getUnauthorizedRedirectUrl() {
        buildGroovyAccessStrategyInstanceIfNeeded();
        return this.groovyStrategyInstance.getUnauthorizedRedirectUrl();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    @JsonIgnore
    public void setServiceAccessAllowed(final boolean enabled) {
        buildGroovyAccessStrategyInstanceIfNeeded();
        this.groovyStrategyInstance.setServiceAccessAllowed(enabled);
    }

    @Override
    @JsonIgnore
    public RegisteredServiceDelegatedAuthenticationPolicy getDelegatedAuthenticationPolicy() {
        buildGroovyAccessStrategyInstanceIfNeeded();
        return this.groovyStrategyInstance.getDelegatedAuthenticationPolicy();
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    private void buildGroovyAccessStrategyInstanceIfNeeded() {
        try {
            if (this.groovyStrategyInstance == null) {
                final Resource groovyResource = ResourceUtils.getResourceFrom(this.groovyScript);
                this.groovyStrategyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceAccessStrategy.class);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
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
        final GroovyRegisteredServiceAccessStrategy rhs = (GroovyRegisteredServiceAccessStrategy) obj;
        return new EqualsBuilder()
            .append(this.order, rhs.order)
            .append(this.groovyScript, rhs.groovyScript)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(order)
            .append(groovyScript)
            .toHashCode();
    }
}
