package org.apereo.cas.util.junit;

import org.apereo.cas.util.SocketUtils;

import lombok.SneakyThrows;
import lombok.val;
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
        val ignore = ((ParentRunner<Object>) runner).getTestClass().getAnnotation(ConditionalIgnore.class);

        if (shouldRunTestsWithCondition(ignore)) {
            super.runChild(runner, notifier);
        }
    }

    @SneakyThrows
    private boolean shouldRunTestsWithCondition(final ConditionalIgnore ignore) {
        if (ignore == null) {
            return true;
        }
        var shouldRun = false;

        if (ignore.condition() == null) {
            shouldRun = true;
        } else {
            val condition = ignore.condition().getDeclaredConstructor().newInstance();
            shouldRun = condition.isSatisfied();
        }

        if (shouldRun && ignore.port() > 0) {
            shouldRun = !SocketUtils.isTcpPortAvailable(ignore.port());
        }
        return shouldRun;
    }
}
