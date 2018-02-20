package org.apereo.cas.util.junit;

/**
 * This is {@link RunningStandaloneCondition}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RunningStandaloneCondition extends RunningContinuousIntegrationCondition {
    @Override
    public boolean isSatisfied() {
        return !super.isSatisfied();
    }
}
