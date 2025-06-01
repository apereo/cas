package org.apereo.cas.grouper.services;

import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.WatcherService;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
@Tag("Grouper")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class GrouperRegisteredServiceAccessStrategyTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    void checkAccessStrategyJson() {
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
    void checkGrouperAttributes() {
        val strategy = new GrouperRegisteredServiceAccessStrategy() {
            @Serial
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
        requiredAttributes.put(GrouperRegisteredServiceAccessStrategy.GROUPER_GROUPS_ATTRIBUTE_NAME, Set.of("SampleGroup"));
        strategy.setRequiredAttributes(requiredAttributes);
        assertTrue(executeStrategy(strategy));
    }

    @Test
    void checkGrouperNoGroups() {
        val strategy = new GrouperRegisteredServiceAccessStrategy() {
            @Serial
            private static final long serialVersionUID = 8533229193475808261L;

            @Override
            protected Collection<WsGetGroupsResult> fetchWsGetGroupsResults(final String principal) {
                return List.of();
            }
        };
        assertFalse(executeStrategy(strategy));
    }

    @Test
    void checkFailsConfig() {
        val strategy = new GrouperRegisteredServiceAccessStrategy();
        strategy.getConfigProperties().put("grouperClient.webService.url", "http://localhost:8012");
        strategy.getConfigProperties().put("grouperClient.webService.login", "unknown");
        strategy.getConfigProperties().put("grouperClient.webService.password", "unknown");
        assertFalse(executeStrategy(strategy));
    }

    private boolean executeStrategy(final GrouperRegisteredServiceAccessStrategy strategy) {
        return strategy.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder()
            .applicationContext(applicationContext)
            .principalId("banderson").build());
    }
}
