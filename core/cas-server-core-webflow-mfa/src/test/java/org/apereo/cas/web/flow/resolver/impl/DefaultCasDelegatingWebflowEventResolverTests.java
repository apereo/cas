package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasDelegatingWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
class DefaultCasDelegatingWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Test
    void verifyOperationNoCredential() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
    }

    @Test
    void verifyAuthFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val id = UUID.randomUUID().toString();
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        servicesManager.save(registeredService);
        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword(id));
        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
        assertTrue(event.getAttributes().contains(Credential.class.getName()));
        assertTrue(event.getAttributes().contains(WebApplicationService.class.getName()));
    }

    @Test
    void verifyServiceDisallowed() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
    }

    @Test
    void verifyNoAuthn() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(id));
        val event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, event.getId());
    }

}
