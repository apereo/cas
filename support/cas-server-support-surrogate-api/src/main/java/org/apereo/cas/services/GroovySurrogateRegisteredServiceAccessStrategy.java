package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.persistence.Transient;

/**
 * This is {@link GroovySurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class GroovySurrogateRegisteredServiceAccessStrategy extends BaseSurrogateRegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = -3998531629984937388L;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledScript executableScript;

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        if (isSurrogateAuthenticationSession(request)) {
            try {
                buildExecutableScriptIfNeeded();
                val args = new Object[]{request.getPrincipalId(), request.getAttributes(), LOGGER};
                return Boolean.TRUE.equals(executableScript.execute(args, Boolean.class));
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return false;
        }
        return super.authorizeRequest(request);
    }

    protected void buildExecutableScriptIfNeeded() {
        if (executableScript == null) {
            FunctionUtils.doAndHandle(_ -> {
                val location = SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript);
                val resource = ResourceUtils.getResourceFrom(location);
                val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                executableScript = scriptFactory.fromResource(resource);
            });
        }
    }
}
