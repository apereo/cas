package org.apereo.cas.util.junit;

import lombok.SneakyThrows;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
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
}
