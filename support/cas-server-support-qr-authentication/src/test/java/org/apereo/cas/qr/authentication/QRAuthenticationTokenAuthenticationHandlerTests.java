package org.apereo.cas.qr.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class QRAuthenticationTokenAuthenticationHandlerTests {
    @Autowired
    @Qualifier("qrAuthenticationTokenAuthenticationHandler")
    private AuthenticationHandler qrAuthenticationTokenAuthenticationHandler;

    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifySupports() {
        val credential = new QRAuthenticationTokenCredential("token");
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(credential));
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(QRAuthenticationTokenCredential.class));
    }

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(qrAuthenticationTokenAuthenticationHandler);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val credential = new QRAuthenticationTokenCredential(jwt);
        val result = qrAuthenticationTokenAuthenticationHandler.authenticate(credential);
        assertEquals(tgt.getAuthentication().getPrincipal().getId(), result.getPrincipal().getId());
    }

    @Test
    public void verifyFailsOperation() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val credential = new QRAuthenticationTokenCredential(jwt);
        assertThrows(FailedLoginException.class,
            () -> qrAuthenticationTokenAuthenticationHandler.authenticate(credential));
    }
}
