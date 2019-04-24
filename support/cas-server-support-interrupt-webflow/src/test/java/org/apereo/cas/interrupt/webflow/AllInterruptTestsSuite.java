package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.webflow.actions.FinalizeInterruptFlowActionTests;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptActionTests;
import org.apereo.cas.interrupt.webflow.actions.PrepareInterruptViewActionTests;

import org.junit.platform.suite.api.SelectClasses;

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
    FinalizeInterruptFlowActionTests.class
})
public class AllInterruptTestsSuite {
}
