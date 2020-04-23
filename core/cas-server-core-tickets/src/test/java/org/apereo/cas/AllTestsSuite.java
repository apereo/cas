package org.apereo.cas;

import org.apereo.cas.ticket.DefaultTicketCatalogTests;
import org.apereo.cas.ticket.InvalidTicketExceptionTests;
import org.apereo.cas.ticket.ServiceTicketImplTests;
import org.apereo.cas.ticket.TicketGrantingTicketImplTests;
import org.apereo.cas.ticket.TicketSerializersTests;
import org.apereo.cas.ticket.TransientSessionTicketImplTests;
import org.apereo.cas.ticket.UnrecognizableServiceForServiceTicketValidationExceptionTests;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicyTests;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilderTests;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactoryTests;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactoryTests;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactoryTests;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandlerTests;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandlerTests;
import org.apereo.cas.ticket.registry.CachingTicketRegistryTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupportTests;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryTests;
import org.apereo.cas.ticket.registry.DistributedTicketRegistryTests;
import org.apereo.cas.ticket.serialization.DefaultTicketStringSerializationManagerTests;
import org.apereo.cas.util.DefaultUniqueTicketIdGeneratorTests;
import org.apereo.cas.util.GroovyUniqueTicketIdGeneratorTests;
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
    TicketGrantingTicketExpirationPolicyBuilderTests.class,
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
    BaseDelegatingExpirationPolicyTests.class,
    TransientSessionTicketImplTests.class,
    DefaultTicketRegistrySupportTests.class,
    DefaultTransientSessionTicketFactoryTests.class,
    TicketEncryptionDecryptionTests.class,
    DefaultUniqueTicketIdGeneratorTests.class,
    AlwaysExpiresExpirationPolicyTests.class,
    HardTimeoutExpirationPolicyTests.class,
    NeverExpiresExpirationPolicyTests.class,
    DefaultTicketRegistryCleanerTests.class,
    TicketSerializersTests.class,
    Cas20ProxyHandlerTests.class,
    GroovyUniqueTicketIdGeneratorTests.class,
    DefaultTicketCatalogTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
