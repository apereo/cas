package org.apereo.cas.mfa.simple;

import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurerTests;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleSendTokenActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllCasSimpleMultifactorTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    CasSimpleMultifactorAuthenticationProviderTests.class,
    CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests.class,
    CasSimpleSendTokenActionTests.class,
    CasSimpleMultifactorAuthenticationTicketFactoryTests.class,
    CasSimpleMultifactorWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllCasSimpleMultifactorTestsSuite {
}
