
package org.apereo.cas;

import org.apereo.cas.web.flow.CompositeProviderSelectionMultifactorWebflowConfigurerTests;
import org.apereo.cas.web.flow.MultifactorAuthenticationAvailableActionTests;
import org.apereo.cas.web.flow.MultifactorAuthenticationBypassActionTests;
import org.apereo.cas.web.flow.MultifactorAuthenticationFailureActionTests;
import org.apereo.cas.web.flow.MultifactorAuthenticationFailureOpenActionTests;
import org.apereo.cas.web.flow.actions.composite.MultifactorProviderSelectedActionTests;
import org.apereo.cas.web.flow.actions.composite.PrepareMultifactorProviderSelectionActionTests;
import org.apereo.cas.web.flow.authentication.ChainingMultifactorAuthenticationProviderSelectorTests;
import org.apereo.cas.web.flow.authentication.GroovyScriptMultifactorAuthenticationProviderSelectorTests;
import org.apereo.cas.web.flow.authentication.RankedMultifactorAuthenticationProviderSelectorTests;
import org.apereo.cas.web.flow.resolver.impl.CompositeProviderSelectionMultifactorWebflowEventResolverTests;
import org.apereo.cas.web.flow.resolver.impl.DefaultCasDelegatingWebflowEventResolverTests;
import org.apereo.cas.web.flow.resolver.impl.RankedMultifactorAuthenticationProviderWebflowEventResolverTests;
import org.apereo.cas.web.flow.resolver.impl.SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolverTests;

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
    MultifactorAuthenticationBypassActionTests.class,
    MultifactorAuthenticationAvailableActionTests.class,
    MultifactorAuthenticationFailureActionTests.class,
    RequestHeaderMultifactorAuthenticationPolicyEventResolverTests.class,
    RequestParameterMultifactorAuthenticationPolicyEventResolverTests.class,
    GroovyScriptMultifactorAuthenticationPolicyEventResolverTests.class,
    AuthenticationAttributeMultifactorAuthenticationPolicyEventResolverTests.class,
    AdaptiveMultifactorAuthenticationPolicyEventResolverTests.class,
    MultifactorAuthenticationTests.class,
    MultifactorProviderSelectedActionTests.class,
    DefaultCasDelegatingWebflowEventResolverTests.class,
    ServiceTicketRequestWebflowEventResolverTests.class,
    ChainingMultifactorAuthenticationProviderSelectorTests.class,
    RankedMultifactorAuthenticationProviderWebflowEventResolverTests.class,
    PrepareMultifactorProviderSelectionActionTests.class,
    CompositeProviderSelectionMultifactorWebflowEventResolverTests.class,
    MultifactorAuthenticationFailureOpenActionTests.class,
    CompositeProviderSelectionMultifactorWebflowConfigurerTests.class,
    SelectiveMultifactorAuthenticationProviderWebflowEventResolverTests.class,
    GroovyScriptMultifactorAuthenticationProviderSelectorTests.class,
    RankedMultifactorAuthenticationProviderSelectorTests.class,
    RequestSessionAttributeMultifactorAuthenticationPolicyEventResolverTests.class,
    TimedMultifactorAuthenticationPolicyEventResolverTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
