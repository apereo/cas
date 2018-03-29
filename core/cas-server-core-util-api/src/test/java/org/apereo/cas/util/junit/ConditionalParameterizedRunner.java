package org.apereo.cas.util.junit;

import lombok.SneakyThrows;
import org.apereo.cas.util.SocketUtils;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.ParentRunner;

/**
 * This is {@link ConditionalParameterizedRunner}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ConditionalParameterizedRunner extends Parameterized {
    public ConditionalParameterizedRunner(final Class<?> klass) throws Throwable {
        super(klass);
    }

    @Override
    @SneakyThrows
    protected void runChild(final Runner runner, final RunNotifier notifier) {
        var runTests = true;
        final var ignore = ((ParentRunner<Object>) runner).getTestClass().getAnnotation(ConditionalIgnore.class);
        if (ignore != null) {
            final IgnoreCondition condition = ignore.condition().getDeclaredConstructor().newInstance();
            runTests = condition.isSatisfied();

            if (runTests) {
                if (ignore.port() > 0) {
                    runTests = !SocketUtils.isTcpPortAvailable(ignore.port());
                }
            }
        }

        if (runTests) {
            super.runChild(runner, notifier);
        }
    }
}
