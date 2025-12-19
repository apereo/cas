package org.apereo.cas.grouper;

import module java.base;
import org.apereo.cas.BaseGrouperConfigurationTests;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.persondir.GrouperPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.test.CasTestExtension;
import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GrouperPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Grouper")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GrouperPersonAttributeDaoTests.GrouperPersonAttributeDaoTestConfiguration.class,
    BaseGrouperConfigurationTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=casuser",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.phone=123456789"
})
class GrouperPersonAttributeDaoTests {
    @Autowired
    @Qualifier("aggregatingAttributeRepository")
    private PersonAttributeDao aggregatingAttributeRepository;

    @Test
    void verifyOperation() {
        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(aggregatingAttributeRepository)
            .principalId("casuser")
            .build()
            .fromAllAttributeRepositories()
            .retrieve();
        assertTrue(attributes.containsKey("grouperGroups"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("phone"));
    }

    @TestConfiguration(value = "GrouperPersonAttributeDaoTestConfiguration", proxyBeanMethods = false)
    static class GrouperPersonAttributeDaoTestConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

        @Override
        public void configureAttributeRepositoryPlan(final PersonDirectoryAttributeRepositoryPlan plan) {
            val dao = new GrouperPersonAttributeDao() {
                @Override
                protected GcGetGroups getGroupsClient() {
                    val group = mock(WsGroup.class);
                    when(group.getName()).thenReturn("Group1");

                    val result = mock(WsGetGroupsResult.class);
                    when(result.getWsGroups()).thenReturn(Arrays.array(group));
                    
                    val results = mock(WsGetGroupsResults.class);
                    when(results.getResults()).thenReturn(Arrays.array(result));
                    val gc = mock(GcGetGroups.class);
                    when(gc.execute()).thenReturn(results);
                    return gc;
                }
            };
            dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("username"));
            plan.registerAttributeRepositories(List.of(dao));
        }
    }
}
