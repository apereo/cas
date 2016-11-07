package org.apereo.cas;

import org.apereo.cas.ticket.InvalidTicketExceptionTests;
import org.apereo.cas.ticket.ServiceTicketImplTests;
import org.apereo.cas.ticket.TicketGrantingTicketImplTests;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationExceptionTests;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandlerTests;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandlerTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryTests;
import org.apereo.cas.ticket.registry.DistributedTicketRegistryTests;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicyTests;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicyTests;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InvalidTicketExceptionTests.class, ServiceTicketImplTests.class,
        TicketGrantingTicketImplTests.class, UnrecognizableServiceForServiceTicketValidationExceptionTests.class,
        MultiTimeUseOrTimeoutExpirationPolicyTests.class, RememberMeDelegatingExpirationPolicyTests.class,
        ThrottledUseAndTimeoutExpirationPolicyTests.class, TicketGrantingTicketExpirationPolicyTests.class,
        TimeoutExpirationPolicyTests.class, DefaultTicketRegistryTests.class,
        DistributedTicketRegistryTests.class, Cas10ProxyHandlerTests.class,
        Cas20ProxyHandlerTests.class})
public class AllTestsSuite {
}
