package org.apereo.cas.support.saml.web.idp.profile.artifact;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSamlArtifactMapTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLResponse")
@TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
class CasSamlArtifactMapTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifyOperationByParam() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val request = new MockHttpServletRequest();
        request.addParameter(casProperties.getTgc().getName(), tgt.getId());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));

        val authnRequest = getAuthnRequestFor("example");
        samlArtifactMap.put("artifact", "relying-party", "issuer", authnRequest);
        assertTrue(samlArtifactMap.contains("artifact"));
    }

    @Test
    void verifyOperationByStore() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val profile = new BasicUserProfile();
        profile.addAttribute(TicketGrantingTicket.class.getName(), tgt.getId());

        val context = new JEEContext(request, response);
        val profileManager = new ProfileManager(context, samlIdPDistributedSessionStore);
        profileManager.save(true, profile, false);
        
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        val authnRequest = getAuthnRequestFor("example");
        samlArtifactMap.put("artifact", "relying-party", "issuer", authnRequest);
        assertTrue(samlArtifactMap.contains("artifact"));
    }
}
