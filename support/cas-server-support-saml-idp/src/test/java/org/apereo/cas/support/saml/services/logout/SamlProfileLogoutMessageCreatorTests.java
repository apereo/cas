package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.logout.DefaultSingleLogoutRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;

import lombok.val;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link SamlProfileLogoutMessageCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class SamlProfileLogoutMessageCreatorTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void verifyOperation() throws Exception {
        val creator = new SamlProfileSingleLogoutMessageCreator(openSamlConfigBean, servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            casProperties.getAuthn().getSamlIdp());

        val logoutRequest = DefaultSingleLogoutRequest.builder()
            .logoutUrl(new URL("https://sp.example.org/slo"))
            .registeredService(SamlIdPTestUtils.getSamlRegisteredService())
            .service(CoreAuthenticationTestUtils.getWebApplicationService())
            .ticketId("ST-123456789")
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();

        val result = creator.create(logoutRequest);
        assertNotNull(result);
    }
}
