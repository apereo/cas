package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateSessionExpirationPolicyJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class SurrogateSessionExpirationPolicyJsonSerializerTests {
    @Test
    public void verifyOperation() {
        val policy = new SurrogateSessionExpirationPolicy();
        val serializer = new SurrogateSessionExpirationPolicyJsonSerializer();
        val result = serializer.toString(policy);
        assertNotNull(result);
        val newPolicy = serializer.from(result);
        assertNotNull(newPolicy);
        assertEquals(policy, newPolicy);
    }

    private static class SurrogateSessionExpirationPolicyJsonSerializer extends AbstractJacksonBackedStringSerializer<ExpirationPolicy> {
        private static final long serialVersionUID = -7883370764375218898L;

        @Override
        public Class<ExpirationPolicy> getTypeToSerialize() {
            return ExpirationPolicy.class;
        }
    }

}
