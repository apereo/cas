package org.apereo.cas.util.cipher;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.security.KeyPair;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PublicKeyPairCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class PublicKeyPairCipherExecutorTests {

    private static class PublicKeyProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                Arguments.of("classpath:keys/RSA2048Public.key", "classpath:keys/RSA2048Private.key"),
                Arguments.of("classpath:keys/ECsecp256r1Public.key", "classpath:keys/ECsecp256r1Private.key"),
                Arguments.of("classpath:keys/ECsecp384r1Public.key", "classpath:keys/ECsecp384r1Private.key"),
                Arguments.of("classpath:keys/ECsecp521r1Public.key", "classpath:keys/ECsecp521r1Private.key")
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(PublicKeyProvider.class)
    public void verifyActionOneWay(final String secretKeyEncryption, final String secretKeySigning) {
        val cipher1 = new TicketGrantingCookieCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        assertNotNull(cipher1.encode("TestValue"));

        val cipher = new ProtocolTicketCipherExecutor(secretKeyEncryption, secretKeySigning, 0, 0);
        assertNotNull(cipher.encode("TestValue"));
    }

    @ParameterizedTest
    @ArgumentsSource(PublicKeyProvider.class)
    public void verifyPublicKeyPairResource(final String publicKey, final String privateKey) {
        val cipher = new PublicKeyPairCipherExecutor(privateKey, publicKey, privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @ParameterizedTest
    @ArgumentsSource(PublicKeyProvider.class)
    public void verifyPublicKeyPair(final String publicKey, final String privateKey) {
        val kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        val cipher = new PublicKeyPairCipherExecutor(kp, kp);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @ParameterizedTest
    @ArgumentsSource(PublicKeyProvider.class)
    public void verifyPublicKeyPairSigning(final String publicKey, final String privateKey) {
        val kp = new KeyPair(AbstractCipherExecutor.extractPublicKeyFromResource(publicKey),
            AbstractCipherExecutor.extractPrivateKeyFromResource(privateKey));
        val cipher = new PublicKeyPairCipherExecutor(kp);
        val testValue = cipher.encode("Value");
        assertEquals("Value", cipher.decode(testValue));
    }

    @ParameterizedTest
    @ArgumentsSource(PublicKeyProvider.class)
    public void verifyPublicKeyPairSigningOnly(final String publicKey, final String privateKey) {
        val cipher = new PublicKeyPairCipherExecutor(privateKey, publicKey);
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }

    @Test
    public void verifyPublicKeyPairDoesNothing() {
        val cipher = new PublicKeyPairCipherExecutor();
        val testValue = cipher.encode("TestValue");
        assertNotNull(testValue);
        assertEquals("TestValue", cipher.decode(testValue));
    }
}
