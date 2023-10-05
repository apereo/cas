package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import groovy.lang.GroovyObject;
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
    private transient GroovyObject groovyStrategyInstance;

    @Override
    public boolean isServiceAccessAllowed(final RegisteredService registeredService) {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(ScriptingUtils.executeGroovyScript(this.groovyStrategyInstance,
                "isServiceAccessAllowed", new Object[]{registeredService}, Boolean.class, false));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    @Override
    public boolean isServiceAccessAllowedForSso(final RegisteredService registeredService) {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(ScriptingUtils.executeGroovyScript(this.groovyStrategyInstance,
                "isServiceAccessAllowedForSso", new Object[]{registeredService}, Boolean.class, false));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        try {
            buildGroovyAccessStrategyInstanceIfNeeded();
            return Boolean.TRUE.equals(ScriptingUtils.executeGroovyScript(this.groovyStrategyInstance,
                "authorizeRequest", new Object[]{request}, Boolean.class, false));
        } catch (final Throwable throwable) {
            throw UnauthorizedServiceException.wrap(throwable);
        }
    }

    protected void buildGroovyAccessStrategyInstanceIfNeeded() {
        if (this.groovyStrategyInstance == null) {
            val groovyResource = FunctionUtils.doUnchecked(() -> {
                val location = SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript);
                return ResourceUtils.getResourceFrom(location);
            });
            this.groovyStrategyInstance = ScriptingUtils.parseGroovyScript(groovyResource, true);
        }
    }
}
