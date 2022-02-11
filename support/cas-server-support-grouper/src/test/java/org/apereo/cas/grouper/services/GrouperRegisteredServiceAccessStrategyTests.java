package org.apereo.cas.grouper.services;

import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.WatcherService;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link GrouperRegisteredServiceAccessStrategyTests} provides
 * test cases for {@link GrouperRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Tag("RegisteredService")
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
        grouper.setConfigProperties(CollectionUtils.wrap("hello", "world"));
        grouper.setRequiredAttributes(attributes);
        service.setAccessStrategy(grouper);

        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val dao = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        val saved = dao.save(service);
        assertEquals(service, saved);
        assertFalse(dao.load().isEmpty());
    }

    @Test
    public void checkGrouperAttributes() {
        val strategy = new GrouperRegisteredServiceAccessStrategy() {
            private static final long serialVersionUID = 8533229193475808261L;

            @Override
            protected Collection<WsGetGroupsResult> fetchWsGetGroupsResults(final String principal) {
                val group = new WsGroup();
                group.setExtension("GroupExtension");
                group.setDescription("Group Desc");
                group.setName("SampleGroup");
                group.setUuid(UUID.randomUUID().toString());
                val result = new WsGetGroupsResult();
                result.setWsGroups(new WsGroup[]{group});
                return List.of(result);
            }
        };
        val requiredAttributes = new HashMap<String, Set<String>>();
        requiredAttributes.put(GrouperRegisteredServiceAccessStrategy.GROUPER_GROUPS_ATTRIBUTE_NAME, Collections.singleton("SampleGroup"));
        strategy.setRequiredAttributes(requiredAttributes);
        val attrs = (Map) RegisteredServiceTestUtils.getTestAttributes("banderson");
        assertTrue(strategy.doPrincipalAttributesAllowServiceAccess("banderson", attrs));
    }

    @Test
    public void checkGrouperNoGroups() {
        val strategy = new GrouperRegisteredServiceAccessStrategy() {
            private static final long serialVersionUID = 8533229193475808261L;
            @Override
            protected Collection<WsGetGroupsResult> fetchWsGetGroupsResults(final String principal) {
                return List.of();
            }
        };
        val attrs = (Map) RegisteredServiceTestUtils.getTestAttributes("banderson");
        assertFalse(strategy.doPrincipalAttributesAllowServiceAccess("banderson", attrs));
    }

    @Test
    public void checkFailsConfig() {
        val strategy = new GrouperRegisteredServiceAccessStrategy();
        strategy.getConfigProperties().put("grouperClient.webService.url", "http://localhost:8012");
        strategy.getConfigProperties().put("grouperClient.webService.login", "unknown");
        strategy.getConfigProperties().put("grouperClient.webService.password", "unknown");
        val attrs = (Map) RegisteredServiceTestUtils.getTestAttributes("banderson");
        assertFalse(strategy.doPrincipalAttributesAllowServiceAccess("banderson", attrs));
    }
}
