package org.apereo.cas.token;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Tickets")
class JwtBuilderTests {

    @Test
    void verifyServiceKeysLookedUpOncePerToken() {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        service.getProperties().put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY.getPropertyName(),
            new DefaultRegisteredServiceProperty(EncodingUtils.generateJsonWebKey(512)));
        service.getProperties().put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY.getPropertyName(),
            new DefaultRegisteredServiceProperty(EncodingUtils.generateJsonWebKey(256)));
        service.getProperties().put(RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ALG.getPropertyName(),
            new DefaultRegisteredServiceProperty(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));

        val signingKeyLookups = new AtomicInteger();
        final RegisteredServiceJwtTicketCipherExecutor serviceCipher = new RegisteredServiceJwtTicketCipherExecutor() {
            @Override
            public Optional<String> getSigningKey(@Nullable final RegisteredService registeredService) {
                signingKeyLookups.incrementAndGet();
                return super.getSigningKey(registeredService);
            }
        };
        val builder = new JwtBuilder(CipherExecutor.noOpOfSerializableToString(), mock(ApplicationContext.class),
            mock(ServicesManager.class), mock(PrincipalResolver.class), serviceCipher, mock(ServiceFactory.class),
            new CasConfigurationProperties());

        val jwt = builder.build(service, new JWTClaimsSet.Builder().subject("casuser").build());
        assertEquals(1, signingKeyLookups.get());
        signingKeyLookups.set(0);
        val claims = builder.unpack(Optional.of(service), jwt);
        assertEquals(1, signingKeyLookups.get());
        assertEquals("casuser", Objects.requireNonNull(claims).getSubject());
    }

    @Nested
    class DefaultTests extends BaseJwtTokenTicketBuilderTests {
        @Test
        void verifyZonedDateTimeWorks() throws Throwable {
            val tgt = new MockTicketGrantingTicket("casuser");
            val jwt = tokenTicketBuilder.build(tgt, Map.of("date-time", List.of(ZonedDateTime.now(Clock.systemUTC()))));
            assertNotNull(jwt);
        }

        @Test
        void verifyServerPrefixAsAud() throws Throwable {
            val jwt = tokenTicketJwtBuilder.build(JwtBuilder.JwtRequest.builder()
                .serviceAudience(Set.of(casProperties.getServer().getPrefix()))
                .jwtId(UUID.randomUUID().toString())
                .subject("casuser")
                .issuer(casProperties.getServer().getPrefix())
                .build());
            assertNotNull(jwt);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.token.crypto.enabled=false")
    class DefaultCipherDisabled extends BaseJwtTokenTicketBuilderTests {
        @Test
        void verifyUnknownJwt() {
            val jwt = """
                eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsIm9yZy5hcGVyZW8uY2FzLnNlcnZpY2VzLlJlZ2\
                lzdGVyZWRTZXJ2aWNlIjoiMjEzMzI0ODYyMSIsImtpZCI6IjEyMzQ1Njc4OTAifQ\
                .eyJzdWIiOiJjYXN1c2VyIiwic2NvcGUiOiJvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2FzLmV4Y\
                W1wbGUub3JnIiwicmVzcG9uc2VfdHlwZSI6ImNvZGUiL\
                CJyZWRpcmVjdF91cmkiOiJodHRwczpcL1wvYXBlcmVvLmdpdGh1Yi5pbyIsImlhdCI6MTY0NjczMTgxOS\
                wianRpIjoiZWVkY2Q5Y2ItNDA1MS00ODAyLWFmYWUtYmFkMzU1NDNiYjU\
                3IiwiY2xpZW50X2lkIjoiY2xpZW50In0\
                .16XuMcIc68QSLeEfOdP6_hegZac-YI46tVfbeEhu6_fiPH5LxB4OOefTNuf0ST18scya18L3Da\
                QLFQhdQkTneKa9dJt4fHl8POQ-IjpagaVWwFMGWM9VyVo_\
                wd0rHd-1pg-OtnvH8PqSZuVoLm--eS0x7vQOX5IKedTXhACIQRZCq3Rxs9s9q1Rhjxv6hvkgWgrG42i5D6IE\
                Uxs1y-a9HLySm2_pxvg_7PiaNIps85Le9mWSrOf_F761q1pKHIR5INDoItMAHWKgnDLjQg8R1WPCyeq7XMa\
                cKeXDS4dYk0IeJPK1teyKWJrsdRBdzgnLVyM6MaFszHWOLv_U9Uy22g""";

            assertThrows(IllegalArgumentException.class, () -> tokenTicketJwtBuilder.unpack(Optional.empty(), jwt));
        }
    }
}
