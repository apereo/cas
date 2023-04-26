package org.apereo.cas;

import org.apereo.cas.config.CasConfigurationCreatedEventTests;
import org.apereo.cas.config.CasConfigurationDeletedEventTests;
import org.apereo.cas.config.CasConfigurationModifiedEventTests;
import org.apereo.cas.support.events.audit.CasAuditActionContextRecordedEventTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    CasAuditActionContextRecordedEventTests.class,
    CasConfigurationCreatedEventTests.class,
    CasConfigurationDeletedEventTests.class,
    CasConfigurationModifiedEventTests.class
})
@Suite
public class AllTestsSuite {
}
