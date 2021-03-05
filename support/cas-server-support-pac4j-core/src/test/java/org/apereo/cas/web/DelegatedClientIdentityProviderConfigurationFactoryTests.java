package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFactoryTests}.
 *
 * @author Adrian Gonzalez
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class DelegatedClientIdentityProviderConfigurationFactoryTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyRedirectUrl() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.addParameter(CasProtocolConstants.PARAMETER_METHOD, "some-method");
        request.addParameter(casProperties.getLocale().getParamName(), "some-locale");
        request.addParameter(casProperties.getTheme().getParamName(), "some-theme");
        val context = new JEEContext(request, response);

        val service = RegisteredServiceTestUtils.getService("example");
        service.setOriginalUrl("http://service.original.url.com");
        val client = new CasClient(new CasConfiguration());
        val factory = DelegatedClientIdentityProviderConfigurationFactory.builder()
            .casProperties(casProperties)
            .client(client)
            .service(service)
            .webContext(context)
            .build();

        val actual = factory.resolve();

        assertTrue(actual.isPresent());
        assertEquals(client.getName(), actual.get().getName());
        assertEquals("cas", actual.get().getType());
        val redirectUrl = actual.get().getRedirectUrl();
        assertNotNull(redirectUrl);

        assertTrue(redirectUrl.startsWith("clientredirect?"));
        assertTrue(redirectUrl.contains("client_name=" + client.getName()));
        assertTrue(redirectUrl.contains("method=some-method"));
        assertTrue(redirectUrl.contains("locale=some-locale"));
        assertTrue(redirectUrl.contains("theme=some-theme"));
        assertTrue(redirectUrl.contains(EncodingUtils.urlEncode(service.getOriginalUrl())));
    }

    @Test
    public void verifyRedirectUrlCorrectlyEncoded() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val service = RegisteredServiceTestUtils.getService("example");
        service.setOriginalUrl("http://service.original.url.com?response_type=idtoken+token");
        val client = new CasClient(new CasConfiguration());
        client.setCustomProperties(Map.of(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS, "custom-class"));
        val factory = DelegatedClientIdentityProviderConfigurationFactory.builder()
            .casProperties(casProperties)
            .client(client)
            .service(service)
            .webContext(context)
            .build();

        val actual = factory.resolve();
        assertTrue(actual.isPresent());
        val redirectUrl = actual.get().getRedirectUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains(EncodingUtils.urlEncode(service.getOriginalUrl())));
    }
}
