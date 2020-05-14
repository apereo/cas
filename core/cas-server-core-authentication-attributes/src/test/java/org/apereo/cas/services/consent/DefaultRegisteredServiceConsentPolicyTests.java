package org.apereo.cas.services.consent;

import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceConsentPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultRegisteredServiceConsentPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultRegisteredServiceConsentPolicyTests.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeToJson() throws IOException {
        val policyWritten = new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("attr1", "attr2"),
            CollectionUtils.wrapSet("ex-attr1", "ex-attr2"));
        policyWritten.setEnabled(true);

        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceConsentPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
