package org.apereo.cas;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGrouperAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.util.CollectionUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseGrouperConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public abstract class BaseGrouperConfigurationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        GrouperTestConfiguration.class,
        CasGrouperAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "GrouperTestConfiguration", proxyBeanMethods = false)
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
