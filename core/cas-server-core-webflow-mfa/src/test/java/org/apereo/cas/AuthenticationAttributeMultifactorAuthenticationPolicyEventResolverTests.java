package org.apereo.cas;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthenticationAttributeMultifactorAuthenticationPolicyEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.triggers.authentication.global-authentication-attribute-name-triggers=authn-method-dummy",
    "cas.authn.mfa.triggers.authentication.global-authentication-attribute-value-regex=mfa.*"
})
@Tag("WebflowEvents")
public class AuthenticationAttributeMultifactorAuthenticationPolicyEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("authenticationAttributeAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver authenticationAttributeMultifactorAuthenticationPolicyEventResolver;


    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(registeredService);

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        var results = authenticationAttributeMultifactorAuthenticationPolicyEventResolver.resolve(context);
        assertNull(results);

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val authn = RegisteredServiceTestUtils.getAuthentication();
        authn.getAttributes().put("authn-method-dummy", List.of("mfa-dummy"));
        WebUtils.putAuthentication(authn, context);

        results = authenticationAttributeMultifactorAuthenticationPolicyEventResolver.resolve(context);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, results.iterator().next().getId());
    }
}
