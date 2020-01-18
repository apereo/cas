package org.apereo.cas.services;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.Transient;

import java.util.Set;

/**
 * This is {@link GroovyRegisteredServiceMultifactorPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated This component is deprecated as of 6.2.0 and is scheduled to be removed.
 */
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated(since = "6.2.0")
@Slf4j
public class GroovyRegisteredServiceMultifactorPolicy implements RegisteredServiceMultifactorPolicy {
    private static final long serialVersionUID = -3075860754996106437L;

    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient RegisteredServiceMultifactorPolicy groovyPolicyInstance;

    public GroovyRegisteredServiceMultifactorPolicy() {
        AsciiArtUtils.printAsciiArtWarning(LOGGER, getClass().getName() + " is now deprecated and scheduled to be removed in the future.");
    }

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

    @Override
    @JsonIgnore
    public String getBypassPrincipalAttributeName() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getBypassPrincipalAttributeName();
    }

    @Override
    @JsonIgnore
    public String getBypassPrincipalAttributeValue() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getBypassPrincipalAttributeValue();
    }

    @Override
    @JsonIgnore
    public String getScript() {
        buildGroovyMultifactorPolicyInstanceIfNeeded();
        return this.groovyPolicyInstance.getScript();
    }

    @SneakyThrows
    private void buildGroovyMultifactorPolicyInstanceIfNeeded() {
        if (this.groovyPolicyInstance == null) {
            val groovyResource = ResourceUtils.getResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript));
            this.groovyPolicyInstance = ScriptingUtils.getObjectInstanceFromGroovyResource(groovyResource, RegisteredServiceMultifactorPolicy.class);
        }
    }

}
