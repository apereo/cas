package org.apereo.cas.support.events;

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
    CasEventsReportEndpointTests.class
})
@RunWith(JUnitPlatform.class)
public class CasEventsTestsSuite {
}
