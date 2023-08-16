package org.apereo.cas;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGrouperConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.GrouperMultifactorAuthenticationConfiguration;
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
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCookieConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasPersonDirectoryGrouperConfiguration.class,
        GrouperTestConfiguration.class,
        GrouperMultifactorAuthenticationConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration(value = "GrouperTestConfiguration",
                       proxyBeanMethods = false)
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
