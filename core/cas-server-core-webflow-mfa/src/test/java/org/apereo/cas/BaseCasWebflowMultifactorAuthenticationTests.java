package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.StubPersonAttributeDao;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.Map;
import java.util.Set;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseCasWebflowMultifactorAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseCasWebflowMultifactorAuthenticationTests.AuthenticationTestConfiguration.class,
    BaseCasWebflowMultifactorAuthenticationTests.AttributeRepositoryTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreValidationAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    BaseCasWebflowMultifactorAuthenticationTests.GeoLocationServiceTestConfiguration.class
})
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@ExtendWith(CasTestExtension.class)
public abstract class BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
    protected CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @BeforeEach
    void setup() {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("181.88.151.12");
        httpRequest.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        val clientInfo = ClientInfo.from(httpRequest);
        ClientInfoHolder.setClientInfo(clientInfo);
    }
    
    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration {
        @Bean
        public AuthenticationEventExecutionPlanConfigurer surrogateAuthenticationEventExecutionPlanConfigurer() {
            return plan -> plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler(CollectionUtils.wrap("casuser", "Mellon")));
        }
    }

    @TestConfiguration(value = "GeoLocationServiceTestConfiguration", proxyBeanMethods = false)
    static class GeoLocationServiceTestConfiguration {
        @Bean
        public GeoLocationService geoLocationService() throws Throwable {
            val service = mock(GeoLocationService.class);
            val response = new GeoLocationResponse();
            response.addAddress("MSIE");
            when(service.locate(anyString(), any(GeoLocationRequest.class))).thenReturn(response);
            return service;
        }
    }

    @TestConfiguration(value = "AttributeRepositoryTestConfiguration", proxyBeanMethods = false)
    public static class AttributeRepositoryTestConfiguration {
        @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
        @Bean
        public PersonAttributeDao attributeRepository() {
            val attrs = CollectionUtils.wrap(
                "uid", CollectionUtils.wrap("uid"),
                "mail", CollectionUtils.wrap("cas@apereo.org"),
                "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                "groupMembership", CollectionUtils.wrap("adopters"));
            return new StubPersonAttributeDao((Map) attrs);
        }

        @Bean
        @ConditionalOnMissingBean(name = AttributeRepositoryResolver.BEAN_NAME)
        public AttributeRepositoryResolver attributeRepositoryResolver() {
            return query -> Set.of(PersonAttributeDao.WILDCARD);
        }
    }
}
