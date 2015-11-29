package org.jasig.cas.grouper.services;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.JsonServiceRegistryDao;
import org.jasig.cas.services.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * The {@link GrouperRegisteredServiceAccessStrategyRegistryTests} includes
 * tests for making sure the strategy can be configured correctly inside the registry.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GrouperRegisteredServiceAccessStrategyRegistryTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkAccessStrategyJson() throws Exception {

        final Map<String, Set<String>> attributes = new HashMap<>();
        final Set<String> v1 = new HashSet<>();
        v1.add("admin");
        attributes.put("memberOf", v1);

        final AbstractRegisteredService service = TestUtils.getRegisteredService("test");
        final GrouperRegisteredServiceAccessStrategy grouper = new GrouperRegisteredServiceAccessStrategy();
        grouper.setRequiredAttributes(attributes);
        service.setAccessStrategy(grouper);
        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE.getFile());
        dao.save(service);
        dao.load();
    }
}
