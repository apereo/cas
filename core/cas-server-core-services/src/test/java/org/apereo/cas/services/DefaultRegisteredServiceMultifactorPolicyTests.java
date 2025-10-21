package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("MFA")
class DefaultRegisteredServiceMultifactorPolicyTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeADefaultRegisteredServiceMultifactorPolicyToJson() throws IOException {
        val policyWritten = new DefaultRegisteredServiceMultifactorPolicy();
        policyWritten.setPrincipalAttributeNameTrigger("trigger");
        policyWritten.setPrincipalAttributeValueToMatch("attribute");
        val providers = new HashSet<String>();
        providers.add("providerOne");
        policyWritten.setMultifactorAuthenticationProviders(providers);

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceMultifactorPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
