package org.apereo.cas.services;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import javax.persistence.Transient;
import java.util.Set;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class GroovyRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {
    private static final long serialVersionUID = -3075860754996106437L;

    private String groovyScript;

    @JsonIgnore
    @Transient
    private transient RegisteredServiceMultifactorPolicy groovyPolicyInstance;

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

    @SneakyThrows
    private void buildGroovyMultifactorPolicyInstanceIfNeeded() {
        if (this.groovyPolicyInstance == null) {
            val groovyResource = ResourceUtils.getResourceFrom(this.groovyScript);
            this.groovyPolicyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceMultifactorPolicy.class);
        }
    }

}
