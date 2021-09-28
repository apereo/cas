package org.apereo.cas.qr.validation;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultQRAuthenticationTokenValidatorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.qr.json.location=file:${java.io.tmpdir}/cas-qr-devices.json")
public class DefaultQRAuthenticationTokenValidatorServiceTests {
    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("qrAuthenticationTokenValidatorService")
    private QRAuthenticationTokenValidatorService qrAuthenticationTokenValidatorService;

    @Autowired
    @Qualifier("qrAuthenticationDeviceRepository")
    private QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository;

    @BeforeEach
    public void beforeEach() {
        qrAuthenticationDeviceRepository.removeAll();
    }

    @Test
    public void verifyUnknownTicket() {
        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId("unknown-id")
            .serviceAudience("https://example.com/normal/")
            .issuer(casProperties.getServer().getPrefix())
            .build();
        val jwt = jwtBuilder.build(payload);
        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(UUID.randomUUID().toString())
            .build();
        assertThrows(InvalidTicketException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
    }

    @Test
    public void verifyExpiredJwt() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId(tgt.getId())
            .serviceAudience("https://example.com/normal/")
            .issuer(casProperties.getServer().getPrefix())
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).minusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(UUID.randomUUID().toString())
            .build();
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
    }

    @Test
    public void verifyBadSubject() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown")
            .jwtId(tgt.getId())
            .serviceAudience("https://example.com/normal/")
            .issuer(casProperties.getServer().getPrefix())
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(UUID.randomUUID().toString())
            .build();
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
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
            .issuer(casProperties.getServer().getPrefix())
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(UUID.randomUUID().toString())
            .build();
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
    }

    @Test
    public void verifyUnauhzDevice() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val deviceId = UUID.randomUUID().toString();
        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(deviceId)
            .build();
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
    }

    @Test
    public void verifySuccess() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val deviceId = UUID.randomUUID().toString();
        qrAuthenticationDeviceRepository.authorizeDeviceFor(deviceId, tgt.getAuthentication().getPrincipal().getId());

        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(deviceId)
            .build();
        val res = qrAuthenticationTokenValidatorService.validate(request);
        assertNotNull(res.getAuthentication());
    }

    @Test
    public void verifyBadDeviceId() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val deviceId = UUID.randomUUID().toString();
        qrAuthenticationDeviceRepository.authorizeDeviceFor(deviceId, tgt.getAuthentication().getPrincipal().getId());

        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of("mismatch")))
            .build();
        val jwt = jwtBuilder.build(payload);

        val request = QRAuthenticationTokenValidationRequest.builder()
            .registeredService(Optional.empty())
            .token(jwt)
            .deviceId(deviceId)
            .build();
        assertThrows(AuthenticationException.class,
            () -> qrAuthenticationTokenValidatorService.validate(request));
    }

}
