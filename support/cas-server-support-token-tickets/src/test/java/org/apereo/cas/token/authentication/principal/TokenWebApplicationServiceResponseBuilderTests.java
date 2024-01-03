package org.apereo.cas.token.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.config.TokenTicketsConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    CasRegisteredServicesTestConfiguration.class,
    TokenTicketsConfiguration.class,
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    TokenCoreConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryStubConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Tag("Authentication")
class TokenWebApplicationServiceResponseBuilderTests {
    @Autowired
    @Qualifier("webApplicationServiceResponseBuilder")
    private ResponseBuilder<WebApplicationService> responseBuilder;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyDecrypt() throws Throwable {
        val signingSecret = "EihBwA3OuDQMm4gdWzkqRJ87596G7o7a_naJAJipxFoRJbXK7APRcnCA91Y30rJdh4q-C2dmpfV6eNhQT0bR5A";
        val encryptionSecret = "dJ2YpUd-r_Qd7e3nDm79WiIHkqaLT8yZt6nN5eG0YnE";

        val cipher = new JwtTicketCipherExecutor(encryptionSecret, signingSecret, true, 0, 0);
        val result = cipher.decode(cipher.encode("ThisIsValue"));
        assertEquals("ThisIsValue", result);
    }

    @Test
    void verifyBuilderSupport() throws Throwable {
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
    void verifyTokenBuilderWithoutServiceTicket() throws Throwable {
        val result = responseBuilder.build(CoreAuthenticationTestUtils.getWebApplicationService("jwtservice"),
            StringUtils.EMPTY, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(result);
        assertFalse(result.attributes().containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }
}
