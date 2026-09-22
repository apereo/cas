package org.apereo.cas.qr.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationChannelControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
@ResourceLock("qrAuthenticationBrokerChannel")
class QRAuthenticationChannelControllerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Autowired
    @Qualifier(JwtBuilder.TICKET_JWT_BUILDER_BEAN_NAME)
    private JwtBuilder jwtBuilder;

    @Autowired
    @Qualifier("clientInboundChannel")
    private ExecutorSubscribableChannel clientInboundChannel;

    @Autowired
    @Qualifier("brokerChannel")
    private ExecutorSubscribableChannel brokerChannel;

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
    void verifyOK() throws Throwable {
        assertNotNull(clientInboundChannel);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val deviceId = UUID.randomUUID().toString();
        val authentication = Objects.requireNonNull(tgt.getAuthentication());
        val principal = Objects.requireNonNull(authentication.getPrincipal());
        qrAuthenticationDeviceRepository.authorizeDeviceFor(deviceId, principal.getId());

        val payload = JwtBuilder.JwtRequest.builder()
            .subject("casuser")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience(Set.of("https://example.com/normal/"))
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .attributes(Map.of(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, List.of(deviceId)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val channelId = UUID.randomUUID().toString();
        val token = String.format("{\"token\": \"%s\"}", jwt);
        val messages = sendAndCaptureBrokerMessages(token, channelId, deviceId);
        val result = findMessageByDestination(messages, "/qrtopic/%s/verify".formatted(channelId)).orElse(null);
        assertNotNull(result);
        val body = readPayload(result);
        assertEquals(Boolean.TRUE.toString(), body.get("success"));
        assertEquals(jwt, body.get("token"));
        assertEquals(deviceId, body.get("deviceId"));
    }

    @Test
    void verifyFails() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val payload = JwtBuilder.JwtRequest.builder()
            .subject("unknown-user")
            .jwtId(tgt.getId())
            .issuer(casProperties.getServer().getPrefix())
            .serviceAudience(Set.of("https://example.com/normal/"))
            .validUntilDate(DateTimeUtils.dateOf(LocalDate.now(Clock.systemUTC()).plusDays(1)))
            .build();
        val jwt = jwtBuilder.build(payload);

        val channelId = UUID.randomUUID().toString();
        val deviceId = UUID.randomUUID().toString();
        val token = String.format("{\"token\": \"%s\"}", jwt);
        val messages = sendAndCaptureBrokerMessages(token, channelId, deviceId);
        val result = findMessageByDestination(messages, "/qrtopic/%s/verify".formatted(channelId)).orElse(null);
        assertNotNull(result);
        assertEquals("cas.authn.qr.fail", readPayload(result).get("error"));
    }

    @Test
    void verifyMissingHeader() throws Exception {
        assertNotNull(clientInboundChannel);
        var messages = sendAndCaptureBrokerMessages("{}", null, null);
        assertTrue(findMessageByDestinationStartingWith(messages, QRAuthenticationConstants.QR_SIMPLE_BROKER_DESTINATION_PREFIX).isEmpty());
        val noHeaderReply = findMessageByDestination(messages, "/topic/accept").orElse(null);
        assertNotNull(noHeaderReply);
        assertEquals("false", new String((byte[]) noHeaderReply.getPayload(), StandardCharsets.UTF_8));

        val channelId = UUID.randomUUID().toString();
        messages = sendAndCaptureBrokerMessages("{}", channelId, null);
        assertTrue(findMessageByDestination(messages, "/qrtopic/%s/verify".formatted(channelId)).isEmpty());
        val missingDeviceReply = findMessageByDestination(messages, "/topic/accept").orElse(null);
        assertNotNull(missingDeviceReply);
        assertEquals("false", new String((byte[]) missingDeviceReply.getPayload(), StandardCharsets.UTF_8));
    }

    private List<Message<?>> sendAndCaptureBrokerMessages(final String payload,
                                                          @Nullable final String channelId,
                                                          @Nullable final String deviceId) throws Exception {
        val brokerMessages = new CopyOnWriteArrayList<Message<?>>();
        val handled = new CountDownLatch(1);
        val interceptor = new ChannelInterceptor() {
            @Override
            public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
                brokerMessages.add(message);
                if ("/topic/accept".equals(StompHeaderAccessor.wrap(message).getDestination())) {
                    handled.countDown();
                }
                return message;
            }
        };
        brokerChannel.addInterceptor(interceptor);
        try {
            clientInboundChannel.send(buildMessage(payload, channelId, deviceId));
            assertTrue(handled.await(30, TimeUnit.SECONDS),
                "Timed out waiting for the controller to answer on /topic/accept");
            return List.copyOf(brokerMessages);
        } finally {
            brokerChannel.removeInterceptor(interceptor);
        }
    }

    private static Map<String, Object> readPayload(final Message<?> message) {
        try {
            if (message.getPayload() instanceof final Map payload) {
                return (Map<String, Object>) payload;
            }
            if (message.getPayload() instanceof final byte[] payload) {
                return MAPPER.readValue(payload, Map.class);
            }
            return MAPPER.readValue(message.getPayload().toString(), Map.class);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Optional<Message<?>> findMessageByDestination(final List<Message<?>> messages, final String destination) {
        return messages.stream()
            .filter(message -> destination.equals(StompHeaderAccessor.wrap(message).getDestination()))
            .findFirst();
    }

    private static Optional<Message<?>> findMessageByDestinationStartingWith(final List<Message<?>> messages, final String destinationPrefix) {
        return messages.stream()
            .filter(message -> {
                val destination = StompHeaderAccessor.wrap(message).getDestination();
                return destination != null && destination.startsWith(destinationPrefix);
            })
            .findFirst();
    }

    private static Message<String> buildMessage(final String payload,
                                                @Nullable final String channelId,
                                                @Nullable final String deviceId) {
        val accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/qr/accept");
        accessor.setSessionId(UUID.randomUUID().toString());
        accessor.setSessionAttributes(new HashMap<>());
        accessor.setHeader(SimpMessageHeaderAccessor.SESSION_ATTRIBUTES, accessor.getSessionAttributes());
        if (channelId != null) {
            accessor.setNativeHeader(QRAuthenticationConstants.QR_AUTHENTICATION_CHANNEL_ID, channelId);
        }
        if (deviceId != null) {
            accessor.setNativeHeader(QRAuthenticationConstants.QR_AUTHENTICATION_DEVICE_ID, deviceId);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

}
