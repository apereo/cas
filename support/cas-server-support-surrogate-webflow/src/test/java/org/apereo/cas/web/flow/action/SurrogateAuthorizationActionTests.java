package org.apereo.cas.web.flow.action;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuthorizationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class SurrogateAuthorizationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SURROGATE_AUTHORIZATION_CHECK)
    private Action surrogateAuthorizationCheck;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAuthorized() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, surrogateAuthorizationCheck.execute(context).getId());
    }

    @Test
    void verifyNotAuthorized() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true));
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateAttribute", CollectionUtils.wrapSet("someValue")));
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);
        assertThrows(PrincipalException.class, () -> surrogateAuthorizationCheck.execute(context));
    }
}
