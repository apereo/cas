package org.apereo.cas;

import org.apereo.cas.web.flow.SingleSignOnParticipationRequestTests;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SelectClasses({
    SingleSignOnParticipationRequestTests.class,
    SingleSignOnParticipationStrategyTests.class
})
@Suite
public class AllTestsSuite {
}
