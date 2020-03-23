package org.apereo.cas;

import org.apereo.cas.support.events.CasEventsReportEndpointTests;
import org.apereo.cas.support.events.DefaultCasEventListenerTests;
import org.apereo.cas.support.events.LoggingCasEventListenerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class CasEventsTestsSuite {
}
