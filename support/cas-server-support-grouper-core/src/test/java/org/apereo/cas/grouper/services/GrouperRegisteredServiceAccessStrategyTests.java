package org.apereo.cas.grouper.services;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The {@link GrouperRegisteredServiceAccessStrategyTests} provides
 * test cases for {@link GrouperRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GrouperRegisteredServiceAccessStrategyTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouperRegisteredServiceAccessStrategyTests.class);
    
    @Test
    public void checkGrouperAttributes() {
        final ClassPathResource resource = new ClassPathResource("grouper.client.properties");
        if (resource.exists()) {
            final GrouperRegisteredServiceAccessStrategy strategy = new GrouperRegisteredServiceAccessStrategy();
            final Map<String, Set<String>> requiredAttributes = new HashMap<>();
            requiredAttributes.put("memberOf", Collections.singleton("admin"));
            strategy.setRequiredAttributes(requiredAttributes);
            strategy.doPrincipalAttributesAllowServiceAccess("banderson", (Map) RegisteredServiceTestUtils.getTestAttributes());
        } else {
            LOGGER.info("[{}] is not configured. Skipping tests", resource.getFilename());
        }
    }
}
