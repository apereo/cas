package org.apereo.cas.support.wsfederation.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * This is {@link WsFederationNavigationControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
class WsFederationNavigationControllerTests extends AbstractWsFederationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() throws Throwable {
        val config = wsFederationConfigurations.toList().getFirst();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://wsfedservice");
        registeredService.setProperties(Map.of(RegisteredServiceProperty.RegisteredServiceProperties.WSFED_RELYING_PARTY_ID.getPropertyName(),
            new DefaultRegisteredServiceProperty(config.getRelyingPartyIdentifier())));
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        servicesManager.save(registeredService);

        val id = config.getId();
        val result = mockMvc.perform(get(WsFederationNavigationController.ENDPOINT_REDIRECT)
                .param(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
                .param(WsFederationNavigationController.PARAMETER_NAME, id)
                .with(request -> {
                    request.setRemoteAddr("185.86.151.11");
                    request.setLocalAddr("185.88.151.11");
                    request.addHeader("User-Agent", "Mozilla/5.0");
                    return request;
                }))
            .andReturn();
        assertEquals(302, result.getResponse().getStatus());
        val redirectedUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.startsWith(config.getIdentityProviderUrl()));
        val parameters = UriComponentsBuilder.fromUriString(redirectedUrl).build().getQueryParams();
        assertEquals("wsignin1.0", parameters.getFirst("wa"));
        assertEquals(config.getRelyingPartyIdentifier(), EncodingUtils.urlDecode(parameters.getFirst("wtrealm")));
        assertEquals(id, EncodingUtils.urlDecode(parameters.getFirst("wctx")));
        assertEquals(casProperties.getServer().getLoginUrl(), EncodingUtils.urlDecode(parameters.getFirst("wreply")));
    }

    @Test
    void verifyMissingId() throws Exception {
        val id = UUID.randomUUID().toString();
        val result = mockMvc.perform(get(WsFederationNavigationController.ENDPOINT_REDIRECT)
                .param(WsFederationNavigationController.PARAMETER_NAME, id))
            .andReturn();
        assertEquals(403, result.getResponse().getStatus());
    }
}
