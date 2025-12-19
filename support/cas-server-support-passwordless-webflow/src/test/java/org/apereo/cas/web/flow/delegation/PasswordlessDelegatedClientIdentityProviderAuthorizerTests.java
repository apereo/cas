package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BasePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessDelegatedClientIdentityProviderAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
class PasswordlessDelegatedClientIdentityProviderAuthorizerTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessDelegatedClientIdentityProviderAuthorizer")
    private DelegatedClientIdentityProviderAuthorizer authorizer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyNoneDefined() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .allowedDelegatedClients(List.of())
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertTrue(isAuthorized(context));
    }

    @Test
    void verifyDefined() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .allowedDelegatedClients(List.of("CasClient"))
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertTrue(isAuthorized(context));
    }

    @Test
    void verifyUnknown() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .allowedDelegatedClients(List.of("AnotherClient"))
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertFalse(isAuthorized(context));
    }

    @Test
    void verifyNoAccount() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertTrue(isAuthorized(context));
    }


    private boolean isAuthorized(final MockRequestContext context) throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(registeredService);
        return authorizer.isDelegatedClientAuthorizedFor("CasClient", service, context);
    }
}
