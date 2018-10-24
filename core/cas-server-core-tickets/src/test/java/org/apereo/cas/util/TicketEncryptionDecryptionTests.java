package org.apereo.cas.util;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.cipher.BaseBinaryCipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.google.common.io.ByteSource;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link CompressionUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TicketEncryptionDecryptionTests {

    private final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
    private final BaseBinaryCipherExecutor cipher = new TestBinaryCipherExecutor("MTIzNDU2Nzg5MDEyMzQ1Ng==",
        "szxK-5_eJjs-aUj-64MpUZ-GPPzGLhYPLGl0wrYjYNVAGva2P0lLe6UGKGM7k8dWxsOVGutZWgvmY3l5oVPO3w",
        512, 16) {
    };

    @Test
    public void checkSerializationOfTgt() {
        val bytes = SerializationUtils.serializeAndEncodeObject(cipher, tgt);
        val obj = SerializationUtils.decodeAndDeserializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfSt() {
        val st = new MockServiceTicket("serviceid", RegisteredServiceTestUtils.getService(), tgt);
        val bytes = SerializationUtils.serializeAndEncodeObject(cipher, st);
        val obj = SerializationUtils.decodeAndDeserializeObject(bytes, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfStBase64Encode() {
        val st = new MockServiceTicket("serviceid", RegisteredServiceTestUtils.getService(), tgt);
        val bytes = SerializationUtils.serializeAndEncodeObject(cipher, st);
        val string = EncodingUtils.encodeBase64(bytes);
        assertNotNull(string);
        val result = EncodingUtils.decodeBase64(string);
        val obj = SerializationUtils.decodeAndDeserializeObject(result, cipher, Ticket.class);
        assertNotNull(obj);
    }

    @Test
    public void checkSerializationOfTgtByteSource() throws Exception {
        val bytes = ByteSource.wrap(SerializationUtils.serializeAndEncodeObject(cipher, tgt));
        val obj = SerializationUtils.decodeAndDeserializeObject(bytes.read(), cipher, Ticket.class);
        assertNotNull(obj);
    }

    private static class TestBinaryCipherExecutor extends BaseBinaryCipherExecutor {
        TestBinaryCipherExecutor(final String encKey, final String signingKey, final int sKey, final int eKey) {
            super(encKey, signingKey, sKey, eKey, "Test");
        }

        @Override
        protected String getEncryptionKeySetting() {
            return "undefined";
        }

        @Override
        protected String getSigningKeySetting() {
            return "undefined";
        }
    }
}
