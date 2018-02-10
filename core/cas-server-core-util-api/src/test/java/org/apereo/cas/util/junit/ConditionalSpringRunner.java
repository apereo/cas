package org.apereo.cas.util.junit;

import lombok.SneakyThrows;
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
        ConditionalIgnore ignore = frameworkMethod.getDeclaringClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            final IgnoreCondition condition = ignore.condition().getDeclaredConstructor().newInstance();
            return !condition.isSatisfied();
        }
        ignore = frameworkMethod.getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            final IgnoreCondition condition = ignore.condition().getDeclaredConstructor().newInstance();
            return !condition.isSatisfied();
        }
        return super.isTestMethodIgnored(frameworkMethod);
    }

    @Override
    @SneakyThrows
    protected Statement withBeforeClasses(final Statement statement) {
        final ConditionalIgnore ignore = getTestClass().getJavaClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            final IgnoreCondition condition = ignore.condition().getDeclaredConstructor().newInstance();
            if (!condition.isSatisfied()) {
                return new ConditionalIgnoreRule.IgnoreStatement(condition);
            }
        }
        return super.withBeforeClasses(statement);
    }

    @Override
    @SneakyThrows
    protected Statement withAfterClasses(final Statement statement) {
        final ConditionalIgnore ignore = getTestClass().getJavaClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            final IgnoreCondition condition = ignore.condition().getDeclaredConstructor().newInstance();
            if (!condition.isSatisfied()) {
                return new ConditionalIgnoreRule.IgnoreStatement(condition);
            }
        }
        return super.withAfterClasses(statement);
    }
}
