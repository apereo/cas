package org.jasig.cas.grouper.services;

import org.jasig.cas.services.TestUtils;
import org.junit.Test;

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

    @Test
    public void testGrouperAttributes() {
        final GrouperRegisteredServiceAccessStrategy strategy = new GrouperRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> requiredAttributes = new HashMap<>();
        requiredAttributes.put("memberOf", Collections.singleton("admin"));

        strategy.setRequiredAttributes(requiredAttributes);
        strategy.doPrincipalAttributesAllowServiceAccess("GrouperSystem", (Map) TestUtils.getTestAttributes());
    }
}
