
package org.apereo.cas;

import org.apereo.cas.consent.AttributeConsentReportEndpointTests;
import org.apereo.cas.consent.DefaultConsentActivationStrategyDisabledTests;
import org.apereo.cas.consent.DefaultConsentActivationStrategyTests;
import org.apereo.cas.consent.DefaultConsentDecisionBuilderTests;
import org.apereo.cas.consent.DefaultConsentEngineTests;
import org.apereo.cas.consent.GroovyConsentActivationStrategyTests;
import org.apereo.cas.consent.GroovyConsentRepositoryTests;
import org.apereo.cas.consent.InMemoryConsentRepositoryTests;
import org.apereo.cas.consent.JsonConsentRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultConsentEngineTests.class,
    GroovyConsentActivationStrategyTests.class,
    DefaultConsentActivationStrategyTests.class,
    GroovyConsentRepositoryTests.class,
    DefaultConsentActivationStrategyDisabledTests.class,
    DefaultConsentDecisionBuilderTests.class,
    InMemoryConsentRepositoryTests.class,
    JsonConsentRepositoryTests.class,
    AttributeConsentReportEndpointTests.class
})
@Suite
public class AllTestsSuite {
}
