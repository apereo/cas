package org.apereo.cas.util.cipher;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTicketCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Tickets")
public class DefaultTicketCipherExecutorTests {
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
    public void verifyEncryptionKeySizes() {
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
    public void verifyExistingEncryptionKeySizes(final int keySize, final String encryptionKey) {
        val signingKeySecret = "VfYEhlNRkOuG8AaWXQmG0QB7XYsvPwpTF6w8pkucuQ3E8ZMBRyesEPMvuBFyF-8czyvapyrsaTwM49x-JzZAKQ";
        val cipher = new DefaultTicketCipherExecutor(encryptionKey, signingKeySecret,
            "AES", 512, keySize, "webflow");
        val encoded = cipher.encode("ST-1234567890".getBytes(StandardCharsets.UTF_8));
        assertEquals("ST-1234567890", new String(cipher.decode(encoded), StandardCharsets.UTF_8));
    }
}
