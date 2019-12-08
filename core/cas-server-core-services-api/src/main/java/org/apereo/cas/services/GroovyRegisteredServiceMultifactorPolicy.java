package org.apereo.cas.services;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroovyRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {
    private static final long serialVersionUID = -3075860754996106437L;

    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient RegisteredServiceMultifactorPolicy groovyPolicyInstance;

    @JsonIgnore
    @Override
    public Set<String> getMultifactorAuthenticationProviders() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getMultifactorAuthenticationProviders();
    }

    @JsonIgnore
    @Override
    public RegisteredServiceMultifactorPolicyFailureModes getFailureMode() {
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

    @JsonIgnore
    @Override
    public boolean isForceExecution() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.isForceExecution();
    }

    @JsonIgnore
    @Override
    public boolean isBypassTrustedDeviceEnabled() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.isBypassTrustedDeviceEnabled();
    }

    @SneakyThrows
    private void buildGroovyMultifactorPolicyInstanceIfNeeded() {
        if (this.groovyPolicyInstance == null) {
            val groovyResource = ResourceUtils.getResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript));
            this.groovyPolicyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceMultifactorPolicy.class);
        }
    }

}
