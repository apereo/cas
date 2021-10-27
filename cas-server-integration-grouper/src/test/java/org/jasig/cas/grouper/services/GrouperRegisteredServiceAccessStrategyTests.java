package org.jasig.cas.grouper.services;

import org.jasig.cas.services.TestUtils;
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
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
}
