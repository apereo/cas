package org.jasig.cas.services;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategyTests} is responsible for
 * running test cases for {@link TimeBasedRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TimeBasedRegisteredServiceAccessStrategyTests {

    @Test
    public void checkAuthorizationByRangePass() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().toString());
        authz.setEndingDateTime(DateTime.now().plusMinutes(10).toString());
        assertTrue(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangeFailStartTime() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().plusDays(1).toString());
        authz.setEndingDateTime(DateTime.now().plusMinutes(10).toString());
        assertFalse(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangePassEndTime() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().toString());
        authz.setEndingDateTime(DateTime.now().plusSeconds(30).toString());
        assertTrue(authz.isServiceAccessAllowed());
    }
}
