package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.WebContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFactoryTests}.
 *
 * @author Adrian Gonzalez
 * @since 6.2.0
 */
public class DelegatedClientIdentityProviderConfigurationFactoryTests {

    private CasConfigurationProperties config;
    private WebContext webContextMock;
    private CasClient client;
    private SimpleWebApplicationServiceImpl service;

    @BeforeEach
    public void setUp() {
        config = new CasConfigurationProperties();
        webContextMock = mock(WebContext.class);
        service = new SimpleWebApplicationServiceImpl();
        client = new CasClient(new CasConfiguration());
    }

    @Test
    public void verifyRedirectUrl() {
        val method = "some-method";
        when(webContextMock.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)).thenReturn(Optional.of(method));
        val locale = "some-locale";
        when(webContextMock.getRequestParameter(config.getLocale().getParamName())).thenReturn(Optional.of(locale));
        val theme = "some-theme";
        when(webContextMock.getRequestParameter(config.getTheme().getParamName())).thenReturn(Optional.of(theme));
        service.setSource("source");
        service.setOriginalUrl("http://service.original.url.com");
        val factory = newFactory();

        Optional<DelegatedClientIdentityProviderConfiguration> actual = factory.resolve();

        assertTrue(actual.isPresent());
        assertEquals(client.getName(), actual.get().getName());
        assertEquals("cas", actual.get().getType());
        val redirectUrl = actual.get().getRedirectUrl();
        assertNotNull(redirectUrl);
        val invalidRedirectUrlMessage = "invalid " + redirectUrl;
        assertTrue(redirectUrl.startsWith("clientredirect?"), invalidRedirectUrlMessage);
        assertTrue(redirectUrl.contains("client_name=" + client.getName()), invalidRedirectUrlMessage);
        assertTrue(redirectUrl.contains("method=" + method), invalidRedirectUrlMessage);
        assertTrue(redirectUrl.contains("locale=" + locale), invalidRedirectUrlMessage);
        assertTrue(redirectUrl.contains("theme=" + theme), invalidRedirectUrlMessage);
        assertTrue(redirectUrl.contains("source=" + URLEncoder.encode(service.getOriginalUrl(), StandardCharsets.UTF_8)),
                invalidRedirectUrlMessage);
    }

    /**
     * check that the + character is encoded correctly
     */
    @Test
    public void verifyRedirectUrlCorrectlyEncoded() {
        service.setSource("source");
        service.setOriginalUrl("http://service.original.url.com?response_type=idtoken+token");
        val factory = newFactory();

        Optional<DelegatedClientIdentityProviderConfiguration> actual = factory.resolve();

        assertTrue(actual.isPresent());
        val redirectUrl = actual.get().getRedirectUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("source=" + URLEncoder.encode(service.getOriginalUrl(), StandardCharsets.UTF_8)));
    }

    private DelegatedClientIdentityProviderConfigurationFactory newFactory() {
        return DelegatedClientIdentityProviderConfigurationFactory.builder()
                    .casProperties(config)
                    .client(client)
                    .service(service)
                    .webContext(webContextMock)
                    .build();
    }
}
