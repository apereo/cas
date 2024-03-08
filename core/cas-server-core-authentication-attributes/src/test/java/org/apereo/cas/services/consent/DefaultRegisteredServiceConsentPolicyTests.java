package org.apereo.cas.services.consent;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceConsentPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceConsentPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeToJson() throws IOException {
        val policyWritten = new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("attr1", "attr2"),
            CollectionUtils.wrapSet("ex-attr1", "ex-attr2"));
        policyWritten.setStatus(TriStateBoolean.TRUE);

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceConsentPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
