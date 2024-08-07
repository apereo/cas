package org.apereo.cas.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseSamlIdPWebflowTests;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML2")
class SamlIdPSingleSignOnParticipationStrategyTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml282")
    class DefaultTests extends BaseSamlIdPWebflowTests {
        @Autowired
        @Qualifier("samlIdPSingleSignOnParticipationStrategy")
        private SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy;

        @Test
        void verifyParticipation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val issuer = UUID.randomUUID().toString();
            val authnRequest = getAuthnRequestFor(issuer);
            val ssoRequest = SingleSignOnParticipationRequest.builder()
                .httpServletRequest(context.getHttpServletRequest())
                .httpServletResponse(context.getHttpServletResponse())
                .requestContext(context)
                .build()
                .attribute(AuthnRequest.class.getName(), authnRequest)
                .attribute(Issuer.class.getName(), issuer);

            assertTrue(samlIdPSingleSignOnParticipationStrategy.supports(ssoRequest));
            assertTrue(samlIdPSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
        }

        @Test
        void verifyForcedAuthn() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val issuer = UUID.randomUUID().toString();
            val authnRequest = getAuthnRequestFor(issuer);
            when(authnRequest.isForceAuthn()).thenReturn(Boolean.TRUE);
            val ssoRequest = SingleSignOnParticipationRequest.builder()
                .httpServletRequest(context.getHttpServletRequest())
                .httpServletResponse(context.getHttpServletResponse())
                .requestContext(context)
                .build()
                .attribute(AuthnRequest.class.getName(), authnRequest)
                .attribute(Issuer.class.getName(), issuer);
            assertTrue(samlIdPSingleSignOnParticipationStrategy.supports(ssoRequest));
            assertFalse(samlIdPSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml989",
        "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy"
    })
    class MfaProviderTests extends BaseSamlIdPWebflowTests {
        @Autowired
        @Qualifier("samlIdPSingleSignOnParticipationStrategy")
        private SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy;

        @Test
        void verifyMfaProviderFailsContext() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val issuer = UUID.randomUUID().toString();
            val authnRequest = getAuthnRequestFor(issuer);
            val ssoRequest = SingleSignOnParticipationRequest.builder()
                .httpServletRequest(context.getHttpServletRequest())
                .httpServletResponse(context.getHttpServletResponse())
                .requestContext(context)
                .build()
                .attribute(AuthnRequest.class.getName(), authnRequest)
                .attribute(Authentication.class.getName(), RegisteredServiceTestUtils.getAuthentication("casuser"))
                .attribute(Issuer.class.getName(), issuer);
            assertFalse(samlIdPSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
        }
    }

}
