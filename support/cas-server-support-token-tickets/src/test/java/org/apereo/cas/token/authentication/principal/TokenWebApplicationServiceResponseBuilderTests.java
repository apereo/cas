package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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
@TestPropertySource(locations = "classpath:tokentests.properties")
public class TokenWebApplicationServiceResponseBuilderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder responseBuilder;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Test
    public void verifyDecrypt() {
        val signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        val encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";

        val cipher = new TokenTicketCipherExecutor(encryptionSecret, signingSecret, true, 0, 0);
        val result = cipher.decode(cipher.encode("ThisIsValue"));
        assertEquals("ThisIsValue", result);
    }

    @Test
    public void verifyBuilderSupport() {
        assertTrue(responseBuilder.supports(serviceFactory.createService("test")));
    }

    @Test
    public void verifyTokenBuilder() {
        val data = "yes\ncasuser";
        try (val webServer = new MockWebServer(8281,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val result = responseBuilder.build(CoreAuthenticationTestUtils.getWebApplicationService("jwtservice"),
                "ST-123456",
                CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(result);
            assertTrue(result.getAttributes().containsKey(CasProtocolConstants.PARAMETER_TICKET));
            val ticket = result.getAttributes().get(CasProtocolConstants.PARAMETER_TICKET);
            assertNotNull(JWTParser.parse(ticket));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }


    }
}
