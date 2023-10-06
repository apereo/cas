package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuthorizationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
class SurrogateAuthorizationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SURROGATE_AUTHORIZATION_CHECK)
    private Action surrogateAuthorizationCheck;

    @Test
    void verifyAuthorized() throws Throwable {
        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, surrogateAuthorizationCheck.execute(context).getId());
    }

    @Test
    void verifyNotAuthorized() throws Throwable {
        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true));
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val p = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(p), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateEnabled(true);
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateAttribute", CollectionUtils.wrapSet("someValue")));
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertThrows(PrincipalException.class, () -> surrogateAuthorizationCheck.execute(context));
    }
}
