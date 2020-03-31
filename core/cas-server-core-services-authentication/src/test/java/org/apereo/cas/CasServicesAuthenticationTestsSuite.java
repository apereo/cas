package org.apereo.cas;

import org.apereo.cas.authentication.WebApplicationServiceResponseBuilderTests;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoderTests;
import org.apereo.cas.authentication.support.ProtocolAttributeEncoderTests;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutorTests;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasServicesAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DefaultCasProtocolAttributeEncoderTests.class,
    ProtocolAttributeEncoderTests.class,
    RegisteredServicePublicKeyCipherExecutorTests.class,
    WebApplicationServiceResponseBuilderTests.class
})
@RunWith(JUnitPlatform.class)
public class CasServicesAuthenticationTestsSuite {
}
