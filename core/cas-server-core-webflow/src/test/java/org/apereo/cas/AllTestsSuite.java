package org.apereo.cas;

import org.apereo.cas.web.flow.ChainingSingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.DefaultLoginWebflowConfigurerRememberMeTests;
import org.apereo.cas.web.flow.DefaultLoginWebflowConfigurerTests;
import org.apereo.cas.web.flow.DefaultLogoutWebflowConfigurerTests;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerActionTests;
import org.apereo.cas.web.flow.actions.CasDefaultFlowUrlHandlerTests;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestActionTests;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialActionTests;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersActionTests;
import org.apereo.cas.web.flow.actions.RedirectToServiceActionTests;
import org.apereo.cas.web.flow.actions.RenewAuthenticationRequestCheckActionTests;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.GroovyCasWebflowAuthenticationExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.configurer.CasWebflowConfigurerTests;
import org.apereo.cas.web.flow.configurer.GroovyWebflowConfigurerTests;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionKeyTests;
import org.apereo.cas.web.flow.executor.WebflowCipherBeanTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    InjectResponseHeadersActionTests.class,
    CasDefaultFlowUrlHandlerTests.class,
    DefaultLoginWebflowConfigurerTests.class,
    DefaultLogoutWebflowConfigurerTests.class,
    RedirectToServiceActionTests.class,
    DefaultLoginWebflowConfigurerRememberMeTests.class,
    CheckWebAuthenticationRequestActionTests.class,
    ClientFlowExecutionKeyTests.class,
    GroovyWebflowConfigurerTests.class,
    ClearWebflowCredentialActionTests.class,
    CasWebflowConfigurerTests.class,
    WebflowCipherBeanTests.class,
    GroovyCasWebflowAuthenticationExceptionHandlerTests.class,
    RenewAuthenticationRequestCheckActionTests.class,
    DefaultCasWebflowAbstractTicketExceptionHandlerTests.class,
    DefaultCasWebflowAuthenticationExceptionHandlerTests.class,
    GenericCasWebflowExceptionHandlerTests.class,
    RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests.class,
    DefaultSingleSignOnParticipationStrategyTests.class,
    ChainingSingleSignOnParticipationStrategyTests.class,
    AuthenticationExceptionHandlerActionTests.class
})
@Suite
public class AllTestsSuite {
}
