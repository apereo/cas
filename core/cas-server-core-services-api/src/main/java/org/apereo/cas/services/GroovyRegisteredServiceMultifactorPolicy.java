package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;
import javax.persistence.Transient;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
public class GroovyRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {

    private static final long serialVersionUID = -3075860754996106437L;

    private String groovyScript;

    @JsonIgnore
    @Transient
    private transient RegisteredServiceMultifactorPolicy groovyPolicyInstance;

    public GroovyRegisteredServiceMultifactorPolicy() {
    }

    @JsonIgnore
    @Override
    public Set<String> getMultifactorAuthenticationProviders() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getMultifactorAuthenticationProviders();
    }

    @JsonIgnore
    @Override
    public FailureModes getFailureMode() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getFailureMode();
    }

    @JsonIgnore
    @Override
    public String getPrincipalAttributeNameTrigger() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getPrincipalAttributeNameTrigger();
    }

    @JsonIgnore
    @Override
    public String getPrincipalAttributeValueToMatch() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getPrincipalAttributeValueToMatch();
    }

    @JsonIgnore
    @Override
    public boolean isBypassEnabled() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.isBypassEnabled();
    }

    private void buildGroovyMultifactorPolicyInstanceIfNeeded() {
        try {
            if (this.groovyPolicyInstance == null) {
                final Resource groovyResource = ResourceUtils.getResourceFrom(this.groovyScript);
                this.groovyPolicyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceMultifactorPolicy.class);
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
        final GroovyRegisteredServiceMultifactorPolicy rhs = (GroovyRegisteredServiceMultifactorPolicy) obj;
        return new EqualsBuilder().append(this.groovyScript, rhs.groovyScript).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(groovyScript).toHashCode();
    }
}
