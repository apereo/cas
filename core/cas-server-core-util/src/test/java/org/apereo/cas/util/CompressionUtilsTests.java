package org.apereo.cas.util;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompressionUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("Utility")
class CompressionUtilsTests {

    @Test
    void verifyDeflation() throws Throwable {
        val source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        val deflated = CompressionUtils.deflateToByteArray(source);
        val results = CompressionUtils.inflateToByteArray(deflated);
        assertEquals(source, new String(Objects.requireNonNull(results), StandardCharsets.UTF_8));
        assertNotNull(CompressionUtils.compress(source.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void verifyDeflationFormats() throws Throwable {
        val oneBitStatusList = new byte[]{(byte) 0xb9, (byte) 0xa3};
        assertEquals("eNrbuRgAAhcBXQ", EncodingUtils.encodeUrlSafeBase64(CompressionUtils.deflate(oneBitStatusList, false)));
        val twoBitStatusList = new byte[]{(byte) 0xc9, 0x44, (byte) 0xf9};
        assertEquals("eNo76fITAAPfAgc", EncodingUtils.encodeUrlSafeBase64(CompressionUtils.deflate(twoBitStatusList, false)));

        val content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".repeat(50).getBytes(StandardCharsets.UTF_8);
        for (val raw : List.of(true, false)) {
            val deflated = CompressionUtils.deflate(content, raw);
            val prefixed = new byte[deflated.length + 3];
            System.arraycopy(deflated, 0, prefixed, 3, deflated.length);
            assertArrayEquals(content, CompressionUtils.inflate(prefixed, 3, raw));
            assertThrows(DataFormatException.class,
                () -> CompressionUtils.inflate(Arrays.copyOf(deflated, deflated.length / 2), 0, raw));
        }
    }

    @Test
    void verifyInflation() {
        val source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                     + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
                     + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
                     + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
                     + "ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        val deflated = CompressionUtils.deflate(source);
        val decoded = EncodingUtils.decodeBase64(deflated);
        val results = CompressionUtils.inflateToString(decoded);
        assertEquals(source, results);

        assertNotNull(CompressionUtils.deflate(source.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void verifyStringCompression() {
        val srcTxt =
            "lamEiLCJhZG1pbiI6dHJ1ZX0.|..03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757|eyJhbGciO"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "iJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzY290Y2guaW8iLCJleHAiOjEzMDA4MTkzODAsIm5hbWUiOiJDaHJpcyBTZXZpbG"
            + "xlamEiLCJhZG1pbiI6dHJ1ZX0.03f329983b86f7d9a9f5fef85305880101d5e302afafa20154d094b229f757";

        val str = CompressionUtils.compress(srcTxt);
        assertNotNull(str);

        val originalStr = CompressionUtils.decompress(str);
        assertNotNull(originalStr);

        assertEquals(srcTxt, originalStr);
    }
}
