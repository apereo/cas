package org.apereo.cas.qr.validation;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultQRAuthenticationTokenValidatorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class DefaultQRAuthenticationTokenValidatorServiceTests {
    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("qrAuthenticationTokenValidatorService")
    private QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService;

    @Test
    public void verifyUnknownTicket() {
        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId("unknown-id")
            .serviceAudience("https://example.com/normal/")
            .build();
        val jwt = jwtBuilder.build(payload);
        assertThrows(InvalidTicketException.class,
            () -> qrAuthenticationTokenValidatorService.validate(Optional.empty(), jwt));
    }

    @Test
    public void verifyExpiredJwt() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId(tgt.getId())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).minusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(Optional.empty(), jwt));
    }

    @Test
    public void verifyBadSubject() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown")
            .jwtId(tgt.getId())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(Optional.empty(), jwt));
    }

    @Test
    public void verifyBadIssuer() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer("unknown")
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(Optional.empty(), jwt));
    }

    @Test
    public void verifySuccess() {
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
        val res = qrAuthenticationTokenValidatorService.validate(Optional.empty(), jwt);
        assertNotNull(res.getAuthentication());
    }


}
