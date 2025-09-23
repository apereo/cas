package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;

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

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        if (isSurrogateAuthenticationSession(request)) {
            try {
                val args = new Object[]{request.getPrincipalId(), request.getAttributes(), LOGGER};
                val resource = ResourceUtils.getResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript));
                val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                return scriptFactory.fromResource(resource).execute(args, Boolean.class);
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return false;
        }
        return super.authorizeRequest(request);
    }
}
