package org.apereo.cas;

import org.apereo.cas.authentication.WebApplicationServiceResponseBuilderTests;
import org.apereo.cas.authentication.principal.DefaultResponseTests;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategyTests;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoderTests;
import org.apereo.cas.authentication.support.ProtocolAttributeEncoderTests;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CasServicesAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DefaultCasProtocolAttributeEncoderTests.class,
    DefaultResponseTests.class,
    ProtocolAttributeEncoderTests.class,
    DefaultServiceMatchingStrategyTests.class,
    RegisteredServicePublicKeyCipherExecutorTests.class,
    WebApplicationServiceResponseBuilderTests.class
})
@Suite
public class CasServicesAuthenticationTestsSuite {
}
