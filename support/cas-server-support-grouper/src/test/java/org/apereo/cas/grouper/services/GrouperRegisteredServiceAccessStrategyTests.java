package org.apereo.cas.grouper.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.TestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * The {@link GrouperRegisteredServiceAccessStrategyTests} provides
 * test cases for {@link GrouperRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GrouperRegisteredServiceAccessStrategyTests {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final File JSON_FILE = new File("grouperRegisteredServiceAccessStrategy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void checkGrouperAttributes() {
        final ClassPathResource resource = new ClassPathResource("grouper.client.properties");
        if (resource.exists()) {
            final GrouperRegisteredServiceAccessStrategy strategy = new GrouperRegisteredServiceAccessStrategy();
            final Map<String, Set<String>> requiredAttributes = new HashMap<>();
            requiredAttributes.put("memberOf", Collections.singleton("admin"));
            strategy.setRequiredAttributes(requiredAttributes);
            strategy.doPrincipalAttributesAllowServiceAccess("banderson", (Map) TestUtils.getTestAttributes());
        } else {
            logger.info("{} is not configured. Skipping tests", resource.getFilename());
        }
    }

    @Test
    public void verifySerializeAGrouperRegisteredServiceAccessStrategyToJson() throws IOException {
        final GrouperRegisteredServiceAccessStrategy strategyWritten = new GrouperRegisteredServiceAccessStrategy();
        final Map<String, Set<String>> requiredAttributes = new HashMap<>();
        requiredAttributes.put("memberOf", Collections.singleton("admin"));
        strategyWritten.setRequiredAttributes(requiredAttributes);
        strategyWritten.doPrincipalAttributesAllowServiceAccess("banderson", (Map) TestUtils.getTestAttributes());

        mapper.writeValue(JSON_FILE, strategyWritten);

        final RegisteredServiceAccessStrategy credentialRead = mapper.readValue(JSON_FILE, GrouperRegisteredServiceAccessStrategy.class);

        assertEquals(strategyWritten, credentialRead);
    }
}
