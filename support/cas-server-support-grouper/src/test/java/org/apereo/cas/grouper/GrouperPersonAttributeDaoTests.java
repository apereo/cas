package org.apereo.cas.grouper;

import org.apereo.cas.BaseGrouperConfigurationTests;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.GrouperPersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GrouperPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
@SpringBootTest(classes = {
    GrouperPersonAttributeDaoTests.GrouperPersonAttributeDaoTestConfiguration.class,
    BaseGrouperConfigurationTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=casuser",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.phone=123456789"
})
public class GrouperPersonAttributeDaoTests {
    @Autowired
    @Qualifier("aggregatingAttributeRepository")
    private IPersonAttributeDao aggregatingAttributeRepository;

    @Test
    public void verifyOperation() {
        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(aggregatingAttributeRepository)
            .principalId("casuser")
            .build()
            .retrieve();
        assertTrue(attributes.containsKey("grouperGroups"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("phone"));
    }

    @TestConfiguration("GrouperPersonAttributeDaoTestConfiguration")
    public static class GrouperPersonAttributeDaoTestConfiguration implements PersonDirectoryAttributeRepositoryPlanConfigurer {

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
