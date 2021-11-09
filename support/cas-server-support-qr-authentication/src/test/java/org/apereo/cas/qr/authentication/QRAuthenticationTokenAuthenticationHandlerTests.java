package org.apereo.cas.qr.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.QRAuthenticationConstants;
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

import javax.security.auth.login.FailedLoginException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class QRAuthenticationTokenAuthenticationHandlerTests {
    @Autowired
    @Qualifier("qrAuthenticationTokenAuthenticationHandler")
    private AuthenticationHandler qrAuthenticationTokenAuthenticationHandler;

    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("qrAuthenticationDeviceRepository")
    private QRAuthenticationDeviceRepository qrAuthenticationDeviceRepository;

    @BeforeEach
    public void beforeEach() {
        qrAuthenticationDeviceRepository.removeAll();
    }


    @Test
    public void verifySupports() {
        val credential = new QRAuthenticationTokenCredential("token", UUID.randomUUID().toString());
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(credential));
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(QRAuthenticationTokenCredential.class));
    }

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(qrAuthenticationTokenAuthenticationHandler);

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
        val credential = new QRAuthenticationTokenCredential(jwt, UUID.randomUUID().toString());
        credential.setDeviceId(deviceId);
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
        val credential = new QRAuthenticationTokenCredential(jwt, UUID.randomUUID().toString());
        assertThrows(FailedLoginException.class,
            () -> qrAuthenticationTokenAuthenticationHandler.authenticate(credential));
    }
}
