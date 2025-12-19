package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Cipher")
class DefaultTicketCipherExecutorTests {
    public static Stream<Arguments> getParameters() {
        return Stream.of(
            Arguments.of(16, "e0q85ep-GXg8tfuDEWUJGw"),
            Arguments.of(32, "7jR4SWiQXhAwZagN1RVNuAJ0PE4R2aeax4Fl6fEtT3A"),
            Arguments.of(128, "Sn80PIjqhVkkKWMKhfjhBA=="),
            Arguments.of(192, "qJ5lc45BTvnJQl+pVp8+scQ/hRSvRvWm"),
            Arguments.of(256, "SEL6UsuoRFnMLzYVt39y40ebQ8ma1sea05uHnfUwOKU=")
        );
    }

    @Test
    void verifyEncryptionKeySizes() {
        IntStream.of(16, 32, 128, 192, 256).forEach(keySize -> {
            val cipher = new DefaultTicketCipherExecutor(null, null,
                "AES", 512, keySize, "webflow");
            val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
            assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
            assertNotNull(cipher.getName());
            assertNotNull(cipher.getSigningKeySetting());
            assertNotNull(cipher.getEncryptionKeySetting());
        });
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    void verifyExistingEncryptionKeySizes(final int keySize, final String encryptionKey) {
        val signingKeySecret = "VfYEhlNRkOuG8AaWXQmG0QB7XYsvPwpTF6w8pkucuQ3E8ZMBRyesEPMvuBFyF-8czyvapyrsaTwM49x-JzZAKQ";
        val cipher = new DefaultTicketCipherExecutor(encryptionKey, signingKeySecret,
            "AES", 512, keySize, "webflow");
        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
    }

    @Test
    void verifyParameterSpecIVConsistency() {
        val signingKey = "GMj2k7oO-tv65hOfz5XPrzjKGtpqzvs9lDyLfhftfoNPjBQUPMwlmP3U6sPsz1NZB-Inc3YvL8rO1k9jYzqUwQ";
        val encryptionKey = "oNNhN4m4hHBrayLpqt9gzA";
        val cipher1 = new DefaultTicketCipherExecutor(encryptionKey, signingKey,
            "AES", 512, 16, "webflow");
        val cipher2 = new DefaultTicketCipherExecutor(encryptionKey, signingKey,
            "AES", 512, 16, "webflow");
        val encoded = cipher1.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher2.decode(encoded), StandardCharsets.UTF_8));

        val cipher3 = new DefaultTicketCipherExecutor(encryptionKey, signingKey,
            "AES", 512, 16, "webflow");
        assertEquals("ST-1234567890", new String(cipher3.decode(encoded), StandardCharsets.UTF_8));
    }

    @Test
    void verifyCompatibilityWithCAS63() {
        val encodedByCas63 = "ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LkViVE0wX0ZzMnhfam9tTzNuMGtpcXU4ODhoZ1JZZlR5bUE5bkRHOGh3aWZCazVuND"
                             + "VtTXVmZy40ZUNYYTluRFl3NnJmeEtHU1Y0c2laajh0aG1Sc1BUd0ZDa3NjMXJRYW8tSGlGVGJ6V1lMcXF4Y080TjVxNENqR3IwM0VYZGNOc3V1cV9ISTRidGVsdw==";
        val encoded = EncodingUtils.decodeBase64(encodedByCas63);
        val signingKey = "RDt6YZHZIH7jUv3nBNIsUMp5Nbs5hblVhK9YYI44KyOXSP5nxpHXD67mH2_7DgklQAPBUcr7WNuOVoeqUFqL6A";
        val encryptionKey = "UdLCpnNxmFOviC3M-kDvvQ";
        val cipher1 = new DefaultTicketCipherExecutor(encryptionKey, signingKey,
            "AES", 512, 16, "cas63");
        val decoded = new String(cipher1.decode(encoded), StandardCharsets.UTF_8);
        assertEquals("CAS_SERVER_VERSION_6.3.X", decoded);
    }
}
