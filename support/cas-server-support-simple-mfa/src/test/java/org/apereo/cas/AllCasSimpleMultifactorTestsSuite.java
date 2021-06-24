package org.apereo.cas;

import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationHandlerTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProviderTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactoryTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilderTests;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorSendTokenActionTests;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasSimpleMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    CasSimpleMultifactorAuthenticationProviderTests.class,
    CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests.class,
    CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilderTests.class,
    CasSimpleMultifactorSendTokenActionTests.class,
    CasSimpleMultifactorAuthenticationHandlerTests.class,
    CasSimpleMultifactorAuthenticationTicketFactoryTests.class,
    CasSimpleMultifactorWebflowConfigurerTests.class
})
@Suite
public class AllCasSimpleMultifactorTestsSuite {
}
