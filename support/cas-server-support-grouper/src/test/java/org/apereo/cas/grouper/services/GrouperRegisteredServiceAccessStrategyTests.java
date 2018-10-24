package org.apereo.cas.grouper.services;

import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * The {@link GrouperRegisteredServiceAccessStrategyTests} provides
 * test cases for {@link GrouperRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class GrouperRegisteredServiceAccessStrategyTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkAccessStrategyJson() throws Exception {
        val attributes = new HashMap<String, Set<String>>();
        val v1 = new HashSet<String>();
        v1.add("admin");
        attributes.put("memberOf", v1);

        val service = RegisteredServiceTestUtils.getRegisteredService("test");
        val grouper = new GrouperRegisteredServiceAccessStrategy();
        grouper.setRequiredAttributes(attributes);
        service.setAccessStrategy(grouper);
        val dao = new JsonServiceRegistry(RESOURCE, false,
            mock(ApplicationEventPublisher.class),
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy());
        dao.save(service);
        dao.load();
    }

    @Test
    public void checkGrouperAttributes() {
        val resource = new ClassPathResource("grouper.client.properties");
        if (resource.exists()) {
            val strategy = new GrouperRegisteredServiceAccessStrategy();
            val requiredAttributes = new HashMap<String, Set<String>>();
            requiredAttributes.put("memberOf", Collections.singleton("admin"));
            strategy.setRequiredAttributes(requiredAttributes);
            strategy.doPrincipalAttributesAllowServiceAccess("banderson", (Map) RegisteredServiceTestUtils.getTestAttributes());
        } else {
            LOGGER.info("[{}] is not configured. Skipping tests", resource.getFilename());
        }
    }
}
