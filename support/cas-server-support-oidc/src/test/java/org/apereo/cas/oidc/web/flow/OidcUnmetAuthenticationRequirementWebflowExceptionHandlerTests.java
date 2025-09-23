package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUnmetAuthenticationRequirementWebflowExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = "cas.http-client.allow-local-urls=true")
class OidcUnmetAuthenticationRequirementWebflowExceptionHandlerTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcUnmetAuthenticationRequirementWebflowExceptionHandler")
    private CasWebflowExceptionHandler oidcUnmetAuthenticationRequirementWebflowExceptionHandler;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        servicesManager.save(getOidcRegisteredService());
        val serviceUrl = "https://localhost:8443/cas?redirect_uri=https://oauth.example.org";
        context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceUrl);

        val authException = new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        assertTrue(oidcUnmetAuthenticationRequirementWebflowExceptionHandler.supports(authException, context));

        val event = oidcUnmetAuthenticationRequirementWebflowExceptionHandler.handle(authException, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, event.getId());
        val url = context.getRequestScope().get("url").toString();
        assertEquals("https://oauth.example.org?error=unmet_authentication_requirements", url);
    }
}
