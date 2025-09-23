package org.apereo.cas.web.flow.executor;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link EncryptedTranscoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1
 */
@Tag("Webflow")
class EncryptedTranscoderTests extends BaseWebflowConfigurerTests {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyEncodeDecode(final boolean compression) throws Exception {
        val transcoder1 = new EncryptedTranscoder(webflowCipherExecutor, compression);
        val encodable = new URI("https://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&"
            + "q=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&aq=&"
            + "sll=38.897678,-77.036517&sspn=0.00835,0.007939&vpsrc=6&t=w&"
            + "g=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&ie=UTF8&hq=&"
            + "hnear=1600+Pennsylvania+Ave+NW,+Washington,+District+of+Columbia,+20500&"
            + "ll=38.898521,-77.036517&spn=0.00835,0.007939&z=17&iwloc=A").toURL();
        val encoded = transcoder1.encode(encodable);
        assertEquals(encodable, transcoder1.decode(encoded));
    }

    @Test
    void verifyBadEncoding() throws Throwable {
        val encoder = new EncryptedTranscoder(mock(CipherExecutor.class));
        assertNotNull(encoder.encode(null));
    }

    @Test
    void verifyNotSerializable() throws Throwable {
        val encoder = new EncryptedTranscoder(mock(CipherExecutor.class));
        assertNull(encoder.encode(new Object()));
    }

    @Test
    void verifyBadDecoding() {
        val encoder = new EncryptedTranscoder(mock(CipherExecutor.class));
        assertThrows(IOException.class, () -> encoder.decode(null));
    }
}

