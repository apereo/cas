package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

/**
 * This is {@link GroovyRegisteredServiceSingleSignOnParticipationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
@RequiredArgsConstructor
@Slf4j
public class GroovyRegisteredServiceSingleSignOnParticipationPolicy implements RegisteredServiceSingleSignOnParticipationPolicy {
    @Serial
    private static final long serialVersionUID = -1223946898337761319L;

    private int order;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledScript executableScript;

    @Override
    public boolean shouldParticipateInSso(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        initializeWatchableScriptIfNeeded();
        return FunctionUtils.doIfNotNull(executableScript, () -> getGroovyScriptResult(registeredService, ticketState), () -> false).get();
    }

    protected Boolean getGroovyScriptResult(final RegisteredService registeredService, final AuthenticationAwareTicket ticketState) {
        return FunctionUtils.doAndHandle(() -> {
            val args = CollectionUtils.<String, Object>wrap("registeredService", registeredService,
                "authentication", ticketState.getAuthentication(), "logger", LOGGER);
            executableScript.setBinding(args);
            return executableScript.execute(args.values().toArray(), Boolean.class);
        }, e -> Boolean.FALSE).get();
    }

    @PostLoad
    private void initializeWatchableScriptIfNeeded() {
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        if (scriptFactoryInstance.isExternalScript(groovyScript)) {
            val script = SpringExpressionLanguageValueResolver.getInstance().resolve(
                scriptFactoryInstance.getExternalScript(groovyScript).orElseThrow()
            );
            val resource = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(script));
            this.executableScript = scriptFactoryInstance.fromResource(resource);
        }
        if (scriptFactoryInstance.isInlineScript(groovyScript) && CasRuntimeHintsRegistrar.notInNativeImage()) {
            val script = scriptFactoryInstance.getInlineScript(groovyScript).orElseThrow();
            this.executableScript = scriptFactoryInstance.fromScript(script);
        }
    }
}
