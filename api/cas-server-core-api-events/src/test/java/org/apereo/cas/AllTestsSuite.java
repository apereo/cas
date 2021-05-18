package org.apereo.cas;

import org.apereo.cas.support.events.audit.CasAuditActionContextRecordedEventTests;
import org.apereo.cas.support.events.config.CasConfigurationCreatedEventTests;
import org.apereo.cas.support.events.config.CasConfigurationDeletedEventTests;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEventTests;

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
