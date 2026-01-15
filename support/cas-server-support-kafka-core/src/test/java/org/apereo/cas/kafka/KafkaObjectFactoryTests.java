package org.apereo.cas.kafka;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link KafkaObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Kafka")
@EnabledIfListeningOnPort(port = 9092)
class KafkaObjectFactoryTests {
    @Test
    void verifyOperation() {
        val factory = new KafkaObjectFactory("localhost:9092");
        assertNotNull(factory.getKafkaAdmin());
        assertNotNull(factory.getKafkaTemplate(new StringSerializer(), new StringSerializer()));
    }
}
