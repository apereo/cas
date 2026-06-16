package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.DecryptionException;
import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebflowConversationStateCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Cipher")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class WebflowConversationStateCipherExecutorTests {
    private static final int GCM_IV_LENGTH = 12;

    private static final int GCM_TAG_BYTE_LENGTH = 16;

    private static final String ENCRYPTION_KEY = "P4fxK62MCY5xL5y1DGb3_Q";

    private static final String SIGNING_KEY = "mpO02yZuW-QowasD_Eo64WsH4Tg75vPqV4KQaI2B5BMiQ-cFm3vHC7lJGJOYToGK6l7Bi_0_jmnZrg8wh1iPZA";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyAction() {
        val cipher = new WebflowConversationStateCipherExecutor("AES", 512, 16);
        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    void verifyCipherWithProps() {
        val cipher = newWebflowCipherExecutor();

        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
        assertNotNull(cipher.getName());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getEncryptionKeySetting());
    }

    @Test
    void verifyCipherWithoutSigning() {
        val cipher = newWebflowCipherExecutor();

        val withoutSigning = cipher.withSigningDisabled();
        val encoded = withoutSigning.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(withoutSigning.decode(encoded), StandardCharsets.UTF_8));
    }

    @Test
    void verifyCipherDoesNotReuseGcmKeystream() {
        val cipher = newWebflowCipherExecutor();

        val first = bytesWithGzipHeader("conversation-state=AAAAAAAAAAAAAAAA");
        val second = bytesWithGzipHeader("conversation-state=BBBBBBBBBBBBBBBB");
        val firstPayload = extractJwsPayload(cipher.encode(first));
        val secondPayload = extractJwsPayload(cipher.encode(second));
        assertEncryptedPayload(firstPayload, first.length);
        assertEncryptedPayload(secondPayload, second.length);

        val plaintextXor = xor(first, second, first.length);
        val ciphertextXor = xor(encryptedBytes(firstPayload, first.length), encryptedBytes(secondPayload, second.length), first.length);
        assertFalse(Arrays.equals(plaintextXor, ciphertextXor),
            "Ciphertexts must not reveal the XOR of their plaintexts");
    }

    @Test
    void verifySamePlaintextProducesDistinctCiphertextAndIv() {
        val cipher = newWebflowCipherExecutor();
        val value = bytesWithGzipHeader("conversation-state=same-value-for-every-attempt");
        val initializationVectors = new LinkedHashSet<String>();
        val payloads = new LinkedHashSet<String>();

        IntStream.range(0, 16).forEach(index -> {
            val encoded = cipher.encode(value);
            val payload = extractJwsPayload(encoded);
            assertEncryptedPayload(payload, value.length);
            assertArrayEquals(value, cipher.decode(encoded));
            initializationVectors.add(EncodingUtils.encodeBase64(initializationVector(payload)));
            payloads.add(EncodingUtils.encodeBase64(payload));
        });

        assertEquals(16, initializationVectors.size(), "Every encryption operation must use a fresh initialization vector");
        assertEquals(16, payloads.size(), "Repeated encryption of the same plaintext must not produce the same payload");
    }

    @ParameterizedTest
    @CsvSource({
        "128, Sn80PIjqhVkkKWMKhfjhBA==",
        "192, qJ5lc45BTvnJQl+pVp8+scQ/hRSvRvWm",
        "256, SEL6UsuoRFnMLzYVt39y40ebQ8ma1sea05uHnfUwOKU="
    })
    void verifyLargerEncryptionKeySizesDoNotReuseInitializationVector(final int encryptionKeySize, final String encryptionKey) {
        val crypto = casProperties.getWebflow().getCrypto();
        val cipher = new WebflowConversationStateCipherExecutor(
            encryptionKey,
            SIGNING_KEY,
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            encryptionKeySize);
        val value = bytesWithGzipHeader("conversation-state=larger-encryption-key");

        val first = extractJwsPayload(cipher.encode(value));
        val second = extractJwsPayload(cipher.encode(value));

        assertEncryptedPayload(first, value.length);
        assertEncryptedPayload(second, value.length);
        assertFalse(Arrays.equals(first, second));
        assertFalse(Arrays.equals(initializationVector(first), initializationVector(second)));
    }

    @Test
    void verifyKnownPlaintextAttemptCannotRecoverOtherPlaintext() {
        val cipher = newWebflowCipherExecutor();
        val knownPlaintext = bytesWithGzipHeader("conversation-state=attacker-known-value");
        val targetPlaintext = bytesWithGzipHeader("conversation-state=victim-secret-value");
        val knownPayload = extractJwsPayload(cipher.encode(knownPlaintext));
        val targetPayload = extractJwsPayload(cipher.encode(targetPlaintext));

        val recoveredKeystream = xor(encryptedBytes(knownPayload, knownPlaintext.length), knownPlaintext, knownPlaintext.length);
        val recoveredTarget = xor(recoveredKeystream, encryptedBytes(targetPayload, targetPlaintext.length), targetPlaintext.length);
        assertFalse(Arrays.equals(targetPlaintext, recoveredTarget),
            "Known plaintext from one webflow state must not recover plaintext from another state");
    }

    @Test
    void verifyEmptyPayloadIsRandomizedAndRoundTrips() {
        val cipher = newWebflowCipherExecutor();
        val first = cipher.encode(ArrayUtils.EMPTY_BYTE_ARRAY);
        val second = cipher.encode(ArrayUtils.EMPTY_BYTE_ARRAY);
        val firstPayload = extractJwsPayload(first);
        val secondPayload = extractJwsPayload(second);

        assertEncryptedPayload(firstPayload, 0);
        assertEncryptedPayload(secondPayload, 0);
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, cipher.decode(first));
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, cipher.decode(second));
        assertFalse(Arrays.equals(firstPayload, secondPayload), "Empty payload encryption must still be randomized");
    }

    @Test
    void verifySignedPayloadTamperingFails() {
        val cipher = newWebflowCipherExecutor();
        val encoded = cipher.encode(bytesWithGzipHeader("conversation-state=tamper-check"));
        assertThrows(DecryptionException.class, () -> cipher.decode(tamperJwsPayload(encoded, 0)));
        assertThrows(DecryptionException.class, () -> cipher.decode(tamperJwsPayload(encoded, GCM_IV_LENGTH)));
        assertThrows(DecryptionException.class, () -> cipher.decode(tamperJwsPayload(encoded, extractJwsPayload(encoded).length - 1)));
    }

    @Test
    void verifyUnsignedPayloadTamperingFailsAuthenticationTagValidation() {
        val cipher = newWebflowCipherExecutor().withSigningDisabled();
        val encoded = cipher.encode(bytesWithGzipHeader("conversation-state=tamper-check"));

        assertThrows(DecryptionException.class, () -> cipher.decode(tamper(encoded, 0)));
        assertThrows(DecryptionException.class, () -> cipher.decode(tamper(encoded, GCM_IV_LENGTH)));
        assertThrows(DecryptionException.class, () -> cipher.decode(tamper(encoded, encoded.length - 1)));
    }

    @Test
    void verifyLegacyCiphertextCanStillBeDecoded() throws Exception {
        val cipher = newWebflowCipherExecutor();
        val withoutSigning = cipher.withSigningDisabled();
        val value = "ST-1234567890".getBytes(StandardCharsets.UTF_8);
        val legacyCipher = Cipher.getInstance("AES/GCM/NoPadding");
        legacyCipher.init(Cipher.ENCRYPT_MODE, cipher.getEncryptionKey(), cipher.getParameterSpec());

        val encoded = legacyCipher.doFinal(value);
        assertArrayEquals(value, withoutSigning.decode(encoded));
    }

    private WebflowConversationStateCipherExecutor newWebflowCipherExecutor() {
        val crypto = casProperties.getWebflow().getCrypto();
        return new WebflowConversationStateCipherExecutor(
            ENCRYPTION_KEY,
            SIGNING_KEY,
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize());
    }

    private static byte[] extractJwsPayload(final byte[] encoded) {
        val token = new String(encoded, StandardCharsets.UTF_8);
        val parts = Splitter.on('.').splitToList(token);
        assertEquals(3, parts.size());
        return EncodingUtils.decodeUrlSafeBase64(parts.get(1));
    }

    private static byte[] tamperJwsPayload(final byte[] encoded, final int index) {
        val token = new String(encoded, StandardCharsets.UTF_8);
        val parts = Splitter.on('.').splitToList(token);
        val payload = EncodingUtils.decodeUrlSafeBase64(parts.get(1));
        val tamperedPayload = tamper(payload, index);
        val tamperedToken = String.join(".", parts.get(0), EncodingUtils.encodeUrlSafeBase64(tamperedPayload), parts.get(2));
        return tamperedToken.getBytes(StandardCharsets.UTF_8);
    }

    private static void assertEncryptedPayload(final byte[] payload, final int plaintextLength) {
        assertEquals(GCM_IV_LENGTH + plaintextLength + GCM_TAG_BYTE_LENGTH, payload.length);
    }

    private static byte[] initializationVector(final byte[] payload) {
        return Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH);
    }

    private static byte[] encryptedBytes(final byte[] payload, final int plaintextLength) {
        return Arrays.copyOfRange(payload, GCM_IV_LENGTH, GCM_IV_LENGTH + plaintextLength);
    }

    private static byte[] bytesWithGzipHeader(final String value) {
        val header = new byte[] {0x1f, (byte) 0x8b, 0x08, 0, 0, 0, 0, 0, 0, 0x03};
        val body = value.getBytes(StandardCharsets.UTF_8);
        val result = new byte[header.length + body.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        return result;
    }

    private static byte[] tamper(final byte[] value, final int index) {
        val result = Arrays.copyOf(value, value.length);
        result[index] ^= 0x01;
        return result;
    }

    private static byte[] xor(final byte[] left, final byte[] right, final int length) {
        val result = new byte[length];
        for (var i = 0; i < length; i++) {
            result[i] = (byte) (left[i] ^ right[i]);
        }
        return result;
    }
}
