package org.apereo.cas.web.flow;

import org.apereo.cas.BaseGrouperConfigurationTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GrouperMultifactorAuthenticationPolicyEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseGrouperConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.triggers.grouper.grouper-group-field=NAME")
@Tag("WebflowEvents")
public class GrouperMultifactorAuthenticationPolicyEventResolverTests {
    @Autowired
    @Qualifier("grouperMultifactorAuthenticationWebflowEventResolver")
    protected CasWebflowEventResolver resolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        val event = resolver.resolve(context);
        assertEquals(1, event.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, event.iterator().next().getId());
    }

    @TestConfiguration("GrouperTestConfiguration")
    @Lazy(false)
    public static class GrouperTestConfiguration {
        @Bean
        public GrouperFacade grouperFacade() {
            val group = new WsGroup();
            group.setName(TestMultifactorAuthenticationProvider.ID);
            group.setDisplayName("Apereo CAS");
            group.setDescription("CAS Authentication with Apereo");
            val result = new WsGetGroupsResult();
            result.setWsGroups(new WsGroup[]{group});
            val facade = mock(GrouperFacade.class);
            when(facade.getGroupsForSubjectId(anyString())).thenReturn(CollectionUtils.wrapList(result));
            return facade;
        }
    }
}
