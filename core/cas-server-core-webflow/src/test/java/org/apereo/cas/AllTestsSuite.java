
package org.apereo.cas;

import org.apereo.cas.web.flow.ChainingSingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.DefaultLoginWebflowConfigurerTests;
import org.apereo.cas.web.flow.DefaultLogoutWebflowConfigurerTests;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerActionTests;
import org.apereo.cas.web.flow.actions.CasDefaultFlowUrlHandlerTests;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialActionTests;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersActionTests;
import org.apereo.cas.web.flow.actions.RedirectToServiceActionTests;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandlerTests;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurerTests;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionKeyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    ClientFlowExecutionKeyTests.class,
    ClearWebflowCredentialActionTests.class,
    AbstractCasWebflowConfigurerTests.class,
    DefaultCasWebflowAbstractTicketExceptionHandlerTests.class,
    DefaultCasWebflowAuthenticationExceptionHandlerTests.class,
    GenericCasWebflowExceptionHandlerTests.class,
    RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests.class,
    DefaultSingleSignOnParticipationStrategyTests.class,
    ChainingSingleSignOnParticipationStrategyTests.class,
    AuthenticationExceptionHandlerActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
