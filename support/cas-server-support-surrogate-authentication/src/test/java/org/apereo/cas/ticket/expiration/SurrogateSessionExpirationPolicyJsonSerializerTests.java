package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateSessionExpirationPolicyJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("ExpirationPolicy")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class SurrogateSessionExpirationPolicyJsonSerializerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val policy = new SurrogateSessionExpirationPolicy();
        val serializer = new SurrogateSessionExpirationPolicyJsonSerializer(applicationContext);
        val result = serializer.toString(policy);
        assertNotNull(result);
        val newPolicy = serializer.from(result);
        assertNotNull(newPolicy);
        assertEquals(policy, newPolicy);
    }

    private static final class SurrogateSessionExpirationPolicyJsonSerializer extends BaseJacksonSerializer<ExpirationPolicy> {
        @Serial
        private static final long serialVersionUID = -7883370764375218898L;

        SurrogateSessionExpirationPolicyJsonSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, ExpirationPolicy.class);
        }
    }

}
