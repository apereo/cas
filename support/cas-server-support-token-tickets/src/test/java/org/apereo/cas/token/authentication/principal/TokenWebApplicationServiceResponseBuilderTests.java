package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasTokenCoreAutoConfiguration;
import org.apereo.cas.config.CasTokenTicketsAutoConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenWebApplicationServiceResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasRegisteredServicesTestConfiguration.class,
    CasTokenTicketsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasTokenCoreAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
class TokenWebApplicationServiceResponseBuilderTests {
    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder<WebApplicationService> responseBuilder;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyDecrypt() {
        val signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        val encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";

        val cipher = new JwtTicketCipherExecutor(encryptionSecret, signingSecret, true, 0, 0);
        cipher.setContentEncryptionAlgorithmIdentifier(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        val result = cipher.decode(cipher.encode("ThisIsValue"));
        assertEquals("ThisIsValue", result);
    }

    @Test
    void verifyBuilderSupport() {
        assertTrue(responseBuilder.supports(serviceFactory.createService("test")));
    }

    @Test
    void verifyTokenBuilder() throws Throwable {
        val service = CoreAuthenticationTestUtils.getWebApplicationService("jwtservice");
        val user = UUID.randomUUID().toString();
        val authentication = CoreAuthenticationTestUtils.getAuthentication(user);
        val tgt = new MockTicketGrantingTicket(authentication);
        val st = new MockServiceTicket("ST-123456", service, tgt);
        ticketRegistry.addTicket(tgt);
        ticketRegistry.addTicket(st);

        val result = responseBuilder.build(service, st.getId(), authentication);
        assertNotNull(result);
        assertTrue(result.attributes().containsKey(CasProtocolConstants.PARAMETER_TICKET));
        val ticket = result.attributes().get(CasProtocolConstants.PARAMETER_TICKET);
        assertNotNull(JWTParser.parse(ticket));
    }

    @Test
    void verifyTokenBuilderWithoutServiceTicket() {
        val result = responseBuilder.build(CoreAuthenticationTestUtils.getWebApplicationService("jwtservice"),
            StringUtils.EMPTY, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(result);
        assertFalse(result.attributes().containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }
}
