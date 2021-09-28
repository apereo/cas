package org.apereo.cas.qr.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link QRAuthenticationChannelControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class QRAuthenticationChannelControllerTests {
    @Autowired
    @Qualifier("tokenTicketJwtBuilder")
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier("qrAuthenticationChannelController")
    private QRAuthenticationChannelController qrAuthenticationChannelController;

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
    public void verifyOK() {
        assertNotNull(qrAuthenticationChannelController);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val deviceId = UUID.randomUUID().toString();
        qrAuthenticationDeviceRepository.authorizeDeviceFor(deviceId, tgt.getAuthentication().getPrincipal().getId());
        
        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val message = mock(Message.class);
        val nativeHeaders = new LinkedMultiValueMap<>();
        nativeHeaders.put(QRAuthenticationConstants.QR_AUTHENTICATION_CHANNEL_ID, List.of(UUID.randomUUID().toString()));
        nativeHeaders.put(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId));
        val headers = new MessageHeaders(Map.of("nativeHeaders", nativeHeaders));
        when(message.getHeaders()).thenReturn(headers);
        val token = String.format("{\"token\": \"%s\"}", jwt);
        when(message.getPayload()).thenReturn(token);
        assertTrue(qrAuthenticationChannelController.verify(message));
    }

    @Test
    public void verifyFails() {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown-user")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience("https://example.com/normal/")
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val message = mock(Message.class);
        val nativeHeaders = new LinkedMultiValueMap<>();
        nativeHeaders.put(QRAuthenticationConstants.QR_AUTHENTICATION_CHANNEL_ID, List.of(UUID.randomUUID().toString()));
        nativeHeaders.put(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(UUID.randomUUID().toString()));
        val headers = new MessageHeaders(Map.of("nativeHeaders", nativeHeaders));
        when(message.getHeaders()).thenReturn(headers);
        val token = String.format("{\"token\": \"%s\"}", jwt);
        when(message.getPayload()).thenReturn(token);
        assertFalse(qrAuthenticationChannelController.verify(message));
    }

    @Test
    public void verifyMissingHeader() {
        assertNotNull(qrAuthenticationChannelController);
        val message = mock(Message.class);
        var headers = new MessageHeaders(Map.of("nativeHeaders", new LinkedMultiValueMap<>()));
        when(message.getHeaders()).thenReturn(headers);
        assertFalse(qrAuthenticationChannelController.verify(message));

        val nativeHeaders = new LinkedMultiValueMap<>();
        nativeHeaders.put(QRAuthenticationConstants.QR_AUTHENTICATION_CHANNEL_ID, List.of(UUID.randomUUID().toString()));
        headers = new MessageHeaders(Map.of("nativeHeaders", nativeHeaders));
        when(message.getHeaders()).thenReturn(headers);
        assertFalse(qrAuthenticationChannelController.verify(message));
    }

}
