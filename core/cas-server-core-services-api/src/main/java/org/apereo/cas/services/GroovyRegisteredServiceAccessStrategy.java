package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import jakarta.persistence.Transient;
import java.io.Serial;

/**
 * This is {@link GroovyRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"order", "groovyScript"}, callSuper = true)
public class GroovyRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {

    @Serial
    private static final long serialVersionUID = -2407494148882123062L;

    private int order;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledScript watchableScript;

    @Override
    public boolean isServiceAccessAllowed(final RegisteredService registeredService, final Service service) {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(watchableScript.execute("isServiceAccessAllowed", Boolean.class, registeredService, service));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    @Override
    public boolean isServiceAccessAllowedForSso(final RegisteredService registeredService) {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(watchableScript.execute("isServiceAccessAllowedForSso", Boolean.class, registeredService));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(watchableScript.execute("authorizeRequest", Boolean.class, request));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    protected void buildGroovyAccessStrategyInstanceIfNeeded() {
        if (watchableScript == null) {
            FunctionUtils.doAndHandle(__ -> {
                val location = SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript);
                val groovyResource = ResourceUtils.getResourceFrom(location);
                val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                watchableScript = scriptFactory.fromResource(groovyResource).setFailOnError(false);
            });
        }
    }
}
