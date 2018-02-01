package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import javax.persistence.Transient;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
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
            final Resource groovyResource = ResourceUtils.getResourceFrom(this.groovyScript);
            this.groovyPolicyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceMultifactorPolicy.class);
        }
    }

}
