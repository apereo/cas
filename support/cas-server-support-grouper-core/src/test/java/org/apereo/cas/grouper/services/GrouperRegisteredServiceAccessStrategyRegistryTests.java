package org.apereo.cas.grouper.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * The {@link GrouperRegisteredServiceAccessStrategyRegistryTests} includes
 * tests for making sure the strategy can be configured correctly inside the registry.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
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

        final AbstractRegisteredService service = RegisteredServiceTestUtils.getRegisteredService("testing");
        final GrouperRegisteredServiceAccessStrategy grouper = new GrouperRegisteredServiceAccessStrategy();
        grouper.setRequiredAttributes(attributes);
        service.setAccessStrategy(grouper);
        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE, false, 
                mock(ApplicationEventPublisher.class), new NoOpRegisteredServiceReplicationStrategy());
        dao.save(service);
        dao.load();
    }
}
