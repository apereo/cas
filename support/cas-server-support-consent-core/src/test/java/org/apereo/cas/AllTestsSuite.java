
package org.apereo.cas;

import org.apereo.cas.consent.AttributeConsentReportEndpointTests;
import org.apereo.cas.consent.DefaultConsentDecisionBuilderTests;
import org.apereo.cas.consent.DefaultConsentEngineTests;
import org.apereo.cas.consent.GroovyConsentRepositoryTests;
import org.apereo.cas.consent.InMemoryConsentRepositoryTests;
import org.apereo.cas.consent.JsonConsentRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultConsentEngineTests.class,
    GroovyConsentRepositoryTests.class,
    DefaultConsentDecisionBuilderTests.class,
    InMemoryConsentRepositoryTests.class,
    JsonConsentRepositoryTests.class,
    AttributeConsentReportEndpointTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
