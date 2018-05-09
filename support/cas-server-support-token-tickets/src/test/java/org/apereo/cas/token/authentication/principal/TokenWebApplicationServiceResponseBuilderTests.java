package org.apereo.cas.token.authentication.principal;

import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.TokenTicketsConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.token.cipher.TokenTicketCipherExecutor;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CasRegisteredServicesTestConfiguration.class,
    TokenTicketsConfiguration.class,
    RefreshAutoConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@Slf4j
@TestPropertySource(locations = "classpath:tokentests.properties")
public class TokenWebApplicationServiceResponseBuilderTests {

    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder responseBuilder;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Test
    public void verifyDecrypt() {
        final String jwt = "eyJhbGciOiJIUzUxMiJ9.WlhsS05tRllRV2xQYVVwRlVsVlphVXhEU21oaVIyTnBUMmxLYT"
            + "JGWVNXbE1RMHBzWW0xTmFVOXBTa0pOVkVrMFVUQktSRXhWYUZSTmFsVXlTVzR3TGk0M2JEVnNWVk5PWW5OTGFYbDNh"
            + "MHhoTUhoUlIxZEJMa053VmpVNVEzTlpTVlprUm5wUVRYbFBjakoyU1MxdVJ6TlVZVGx2Y0ZSM1JUVXdhMnhZWW1sNmNu"
            + "Wm5VM00zUW5OTk1FRTVNWGQxWldobFdFUndUVk5DWkhRek4yaDRlV3RuTjJGUWIydGFkMjlxZEdkQmVUQXpWV0k0UWpkSF"
            + "drc3RSM2d3ZEdsbVN6bDFTMHd3WlVsQ1FsbGFiVzVZWWs1UmRFaFpVMjVyU21GQmRIWjBaWEowVkVscmQxaENTelZyUkRkW"
            + "FFtcDBPVXB6YkVkYVJEaHVVRzVXT0VwaVZXSTRPWFp2Um5SRk0waHhaMUJXY1VkUWVIWlVhalEzYUROeWJsVXliamhhTkdaZl"
            + "JqQnFUMEZ2U2s1Q1IycHNUVTVoWDJ0cVZVazNlVmQxT1dSNmJFVldUVWxyUlVwS05VeGFSa040TTAxR2RqRlpkREZ2VGtGdVZVY3h"
            + "SMmczVUhSS0xUaFFkRWxOT0dSbFpYTTJiSGt5Y1hZMWFWQlFaa2hwVVVGSFduRjFkM0V5YkdwVWVIcFNNSEV0V25sSFNISjNSbFpwV0h"
            + "kdmREUk9UWFJPU1Y5T2JFTnhkMGhOYW5SRFZrSlljMFYwVERsRFpEQjBUMGRqWjNCNWRXeGZTWEJXZEVneFduRlhSbHBPVkZGaldrMVlZ"
            + "alExUzNOdFVURmZabU54UkU5SGNsUlVTa2g1ZEVwS2JWaEdla0pDY3pGUlZVMXdXVWs0TjFwVE9IVnVPSFJ4VlZrdWEzSkNOMFk0T1hKc"
            + "U5td3lhMXB1WVZOcE1WUndVUT09.RFGa_ZuEtvPm7vnl0O3Z5D1waPDIbqxiDFTE8WU5zm7ssKACNPA0hRiJIM0Lo5Vs4ATh06LitXmhzxY5Ix9iyA";

        final String signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        final String encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";

        final TokenTicketCipherExecutor cipher = new TokenTicketCipherExecutor(encryptionSecret, signingSecret, true);
        final String result = cipher.decode(jwt);
        assertNotNull(result);
    }

    @Test
    public void verifyBuilderSupport() {
        assertTrue(responseBuilder.supports(serviceFactory.createService("test")));
    }

    @Test
    public void verifyTokenBuilder() {
        final String data = "yes\ncasuser";
        try (MockWebServer webServer = new MockWebServer(8281,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            final Response result = responseBuilder.build(CoreAuthenticationTestUtils.getWebApplicationService("jwtservice"),
                "ST-123456",
                CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(result);
            assertTrue(result.getAttributes().containsKey(CasProtocolConstants.PARAMETER_TICKET));
            final String ticket = result.getAttributes().get(CasProtocolConstants.PARAMETER_TICKET);
            assertNotNull(JWTParser.parse(ticket));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }


    }
}
