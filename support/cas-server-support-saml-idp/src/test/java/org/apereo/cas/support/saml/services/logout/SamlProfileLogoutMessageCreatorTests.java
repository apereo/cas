package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.logout.DefaultLogoutRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;

import lombok.SneakyThrows;
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

    @SneakyThrows
    @Test
    public void verifyOperation() {
        val creator = new SamlProfileLogoutMessageCreator(openSamlConfigBean, servicesManager,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            casProperties.getAuthn().getSamlIdp());

        val logoutRequest = DefaultLogoutRequest.builder()
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
