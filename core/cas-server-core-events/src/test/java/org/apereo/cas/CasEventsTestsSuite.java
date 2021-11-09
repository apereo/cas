package org.apereo.cas;

import org.apereo.cas.support.events.CasEventsReportEndpointTests;
import org.apereo.cas.support.events.DefaultCasEventListenerTests;
import org.apereo.cas.support.events.LoggingCasEventListenerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CasEventsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    DefaultCasEventListenerTests.class,
    CasEventsReportEndpointTests.class,
    LoggingCasEventListenerTests.class
})
@Suite
public class CasEventsTestsSuite {
}
