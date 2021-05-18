package org.apereo.cas;

import org.apereo.cas.interrupt.CasInterruptConfigurationTests;
import org.apereo.cas.interrupt.webflow.InterruptSingleSignOnParticipationStrategyTests;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurerTests;
import org.apereo.cas.interrupt.webflow.actions.FinalizeInterruptFlowActionTests;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptActionTests;
import org.apereo.cas.interrupt.webflow.actions.PrepareInterruptViewActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllInterruptTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    InterruptSingleSignOnParticipationStrategyTests.class,
    PrepareInterruptViewActionTests.class,
    InquireInterruptActionTests.class,
    CasInterruptConfigurationTests.class,
    InterruptWebflowConfigurerTests.class,
    FinalizeInterruptFlowActionTests.class
})
@Suite
public class AllInterruptTestsSuite {
}
