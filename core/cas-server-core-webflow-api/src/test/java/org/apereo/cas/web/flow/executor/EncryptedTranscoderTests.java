package org.apereo.cas.web.flow.executor;

import lombok.val;
import org.cryptacular.bean.AEADBlockCipherBean;
import org.cryptacular.bean.BufferedBlockCipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.io.FileResource;
import org.cryptacular.spec.AEADBlockCipherSpec;
import org.cryptacular.spec.BufferedBlockCipherSpec;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link EncryptedTranscoder}.
 *
 * @author Misagh Moayyed
 * @since 6.1
 */
public class EncryptedTranscoderTests {

    public static Stream<Arguments> getParameters() throws Exception {
        val ksFactory = new KeyStoreFactoryBean();
        ksFactory.setResource(new FileResource(new File("src/test/resources/test-keystore.jceks")));
        ksFactory.setType("JCEKS");
        ksFactory.setPassword("changeit");

        val transcoder1 = new EncryptedTranscoder();
        val cipherBean1 = new AEADBlockCipherBean();
        cipherBean1.setBlockCipherSpec(new AEADBlockCipherSpec("AES", "GCM"));
        cipherBean1.setKeyStore(ksFactory.newInstance());
        cipherBean1.setKeyAlias("aes128");
        cipherBean1.setKeyPassword("changeit");
        cipherBean1.setNonce(new org.cryptacular.generator.sp80038d.RBGNonce());
        transcoder1.setCipherBean(cipherBean1);
        transcoder1.setCompression(true);

        val transcoder2 = new EncryptedTranscoder();
        val cipherBean2 = new BufferedBlockCipherBean();
        cipherBean2.setBlockCipherSpec(new BufferedBlockCipherSpec("AES", "CBC", "PKCS7"));
        cipherBean2.setKeyStore(ksFactory.newInstance());
        cipherBean2.setKeyAlias("aes128");
        cipherBean2.setKeyPassword("changeit");
        cipherBean2.setNonce(new org.cryptacular.generator.sp80038a.RBGNonce());
        transcoder2.setCipherBean(cipherBean2);
        transcoder2.setCompression(false);

        return Stream.of(
            Arguments.arguments(transcoder1,
                "Four score and seven years ago our forefathers brought forth upon this continent a "
                    + "new nation conceived in liberty and dedicated to the proposition that all men "
                    + "are created equal."),
            Arguments.arguments(transcoder2,
                new URL("https://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&"
                    + "q=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&aq=&"
                    + "sll=38.897678,-77.036517&sspn=0.00835,0.007939&vpsrc=6&t=w&"
                    + "g=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&ie=UTF8&hq=&"
                    + "hnear=1600+Pennsylvania+Ave+NW,+Washington,+District+of+Columbia,+20500&"
                    + "ll=38.898521,-77.036517&spn=0.00835,0.007939&z=17&iwloc=A"))
        );
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyEncodeDecode(final EncryptedTranscoder transcoder,
                                   final Object encodable) throws Exception {
        val encoded = transcoder.encode(encodable);
        assertEquals(encodable, transcoder.decode(encoded));
    }
}

