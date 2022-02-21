package org.apereo.cas;

import org.apereo.cas.support.events.CasAuthenticationEventListenerTests;
import org.apereo.cas.support.events.CasEventsReportEndpointTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CasEventsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasAuthenticationEventListenerTests.class,
    CasEventsReportEndpointTests.class
})
@Suite
public class CasEventsTestsSuite {
}
