package org.apereo.cas.util.junit;

import org.apereo.cas.util.SocketUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is {@link ConditionalSpringRunner}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConditionalSpringRunner extends SpringJUnit4ClassRunner {
    /**
     * Construct a new {@code SpringJUnit4ClassRunner}.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public ConditionalSpringRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    @SneakyThrows
    protected boolean isTestMethodIgnored(final FrameworkMethod frameworkMethod) {
        var ignore = frameworkMethod.getDeclaringClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            return !isIgnoreConditionSatisfied(ignore);
        }
        ignore = frameworkMethod.getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            return !isIgnoreConditionSatisfied(ignore);
        }
        return super.isTestMethodIgnored(frameworkMethod);
    }

    @Override
    @SneakyThrows
    protected Statement withBeforeClasses(final Statement statement) {
        val ignore = getTestClass().getJavaClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            if (!isIgnoreConditionSatisfied(ignore)) {
                val condition = ignore.condition().getDeclaredConstructor().newInstance();
                return new ConditionalIgnoreRule.IgnoreStatement(condition);
            }
        }
        return super.withBeforeClasses(statement);
    }

    @Override
    @SneakyThrows
    protected Statement withAfterClasses(final Statement statement) {
        val ignore = getTestClass().getJavaClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            if (!isIgnoreConditionSatisfied(ignore)) {
                val condition = ignore.condition().getDeclaredConstructor().newInstance();
                return new ConditionalIgnoreRule.IgnoreStatement(condition);
            }
        }
        return super.withAfterClasses(statement);
    }

    private boolean isIgnoreConditionSatisfied(final ConditionalIgnore ignore) throws Exception {
        if (ignore.condition() == null) {
            return !SocketUtils.isTcpPortAvailable(ignore.port());
        }

        val portRunning = !SocketUtils.isTcpPortAvailable(ignore.port());

        val condition = ignore.condition().getDeclaredConstructor().newInstance();
        val satisfied = condition.isSatisfied();
        if (satisfied == null || satisfied) {
            return portRunning;
        }
        return satisfied;
    }
}
