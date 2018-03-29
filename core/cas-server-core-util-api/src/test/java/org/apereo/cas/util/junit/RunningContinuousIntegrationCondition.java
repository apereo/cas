package org.apereo.cas.util.junit;

/**
 * This is {@link RunningContinuousIntegrationCondition}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RunningContinuousIntegrationCondition implements IgnoreCondition {
    @Override
    public boolean isSatisfied() {
        final var sysProp = System.getProperty("CI", Boolean.FALSE.toString());
        final var envProp = System.getenv("CI");
        return "true".equals(sysProp) || "true".equals(envProp);
    }
}
