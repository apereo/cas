package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPConsentSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAML2")
@ImportAutoConfiguration(CasConsentCoreAutoConfiguration.class)
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml12182")
class SamlIdPConsentSingleSignOnParticipationStrategyTests extends BaseSamlIdPWebflowTests {

    @Autowired
    @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
    private SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    @Test
    void verifyIdPNeedsConsentOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "CAS-System"));
        val authn = RegisteredServiceTestUtils.getAuthentication(principal);
        val cookie = new MockTicketGrantingTicket(authn);
        val issuer = UUID.randomUUID().toString();

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService(issuer);
        registeredService.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("uid")));
        val service = RegisteredServiceTestUtils.getService(issuer);

        val authnRequest = getAuthnRequestFor(issuer);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build()
            .attribute(AuthnRequest.class.getName(), authnRequest)
            .attribute(Issuer.class.getName(), issuer)
            .attribute(Service.class.getName(), service)
            .attribute(RegisteredService.class.getName(), registeredService)
            .attribute(Authentication.class.getName(), authn)
            .attribute(TicketGrantingTicket.class.getName(), cookie);
        assertFalse(singleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }
}
