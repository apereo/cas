package org.apereo.cas.qr.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link QRAuthenticationTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class QRAuthenticationTokenAuthenticationHandlerTests {
    @Autowired
    @Qualifier("qrAuthenticationTokenAuthenticationHandler")
    private AuthenticationHandler qrAuthenticationTokenAuthenticationHandler;

    @Autowired
    @Qualifier(JwtBuilder.TICKET_JWT_BUILDER_BEAN_NAME)
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
    void beforeEach() {
        qrAuthenticationDeviceRepository.removeAll();
    }


    @Test
    void verifySupports() {
        val credential = new QRAuthenticationTokenCredential("token", UUID.randomUUID().toString());
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(credential));
        assertTrue(qrAuthenticationTokenAuthenticationHandler.supports(QRAuthenticationTokenCredential.class));
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(qrAuthenticationTokenAuthenticationHandler);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val deviceId = UUID.randomUUID().toString();
        qrAuthenticationDeviceRepository.authorizeDeviceFor(deviceId, tgt.getAuthentication().getPrincipal().getId());

        val payload = JwtBuilder.JwtRequest.builder()
            .subject(tgt.getAuthentication().getPrincipal().getId())
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience(Set.of("https://example.com/normal/"))
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val credential = new QRAuthenticationTokenCredential(jwt, UUID.randomUUID().toString());
        credential.setDeviceId(deviceId);
        val result = qrAuthenticationTokenAuthenticationHandler.authenticate(credential, mock(Service.class));
        assertEquals(tgt.getAuthentication().getPrincipal().getId(), result.getPrincipal().getId());
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience(Set.of("https://example.com/normal/"))
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);
        val credential = new QRAuthenticationTokenCredential(jwt, UUID.randomUUID().toString());
        assertThrows(FailedLoginException.class,
            () -> qrAuthenticationTokenAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }
}
