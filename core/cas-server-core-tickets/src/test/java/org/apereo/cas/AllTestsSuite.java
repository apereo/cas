package org.apereo.cas;

import org.apereo.cas.ticket.InvalidTicketExceptionTests;
import org.apereo.cas.ticket.ServiceTicketImplTests;
import org.apereo.cas.ticket.TicketGrantingTicketImplTests;
import org.apereo.cas.ticket.TicketSerializersTests;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationExceptionTests;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactoryTests;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactoryTests;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandlerTests;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandlerTests;
import org.apereo.cas.ticket.registry.CachingTicketRegistryTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryTests;
import org.apereo.cas.ticket.registry.DistributedTicketRegistryTests;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManagerTests;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicyTests;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicyTests;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicyTests;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicyTests;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicyTests;
import org.apereo.cas.util.DefaultUniqueTicketIdGeneratorTests;
import org.apereo.cas.util.TicketEncryptionDecryptionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    DefaultServiceTicketFactoryTests.class,
    DefaultProxyTicketFactoryTests.class,
    InvalidTicketExceptionTests.class,
    ServiceTicketImplTests.class,
    DefaultTicketStringSerializationManagerTests.class,
    TicketGrantingTicketImplTests.class,
    UnrecognizableServiceForServiceTicketValidationExceptionTests.class,
    MultiTimeUseOrTimeoutExpirationPolicyTests.class,
    RememberMeDelegatingExpirationPolicyTests.class,
    ThrottledUseAndTimeoutExpirationPolicyTests.class,
    TicketGrantingTicketExpirationPolicyTests.class,
    TimeoutExpirationPolicyTests.class,
    DefaultTicketRegistryTests.class,
    CachingTicketRegistryTests.class,
    DistributedTicketRegistryTests.class,
    Cas10ProxyHandlerTests.class,
    TicketEncryptionDecryptionTests.class,
    DefaultUniqueTicketIdGeneratorTests.class,
    AlwaysExpiresExpirationPolicyTests.class,
    HardTimeoutExpirationPolicyTests.class,
    NeverExpiresExpirationPolicyTests.class,
    DefaultTicketRegistryCleanerTests.class,
    TicketSerializersTests.class,
    Cas20ProxyHandlerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
