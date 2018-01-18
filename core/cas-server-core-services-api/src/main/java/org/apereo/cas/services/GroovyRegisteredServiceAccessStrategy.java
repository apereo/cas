package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import javax.persistence.Transient;
import java.net.URI;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class GroovyRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -2407494148882123062L;

    /**
     * The sorting/execution order of this strategy.
     */
    private int order;

    private String groovyScript;

    @JsonIgnore
    @Transient
    private transient RegisteredServiceAccessStrategy groovyStrategyInstance;

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

    @SneakyThrows
    private void buildGroovyAccessStrategyInstanceIfNeeded() {
        if (this.groovyStrategyInstance == null) {
            final Resource groovyResource = ResourceUtils.getResourceFrom(this.groovyScript);
            this.groovyStrategyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceAccessStrategy.class);
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
        return new EqualsBuilder().append(this.order, rhs.order).append(this.groovyScript, rhs.groovyScript).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(order).append(groovyScript).toHashCode();
    }
}
