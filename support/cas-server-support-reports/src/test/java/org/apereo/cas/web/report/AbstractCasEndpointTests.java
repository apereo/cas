package org.apereo.cas.web.report;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLoggingAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasReportsAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.web.servlet.MockMvc;

/**
 * This is {@link AbstractCasEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(
    classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = "management.endpoints.web.exposure.include=*",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@AutoConfigureMockMvc
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class AbstractCasEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    protected MockMvc mockMvc;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    protected AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @TestConfiguration(value = "AuditTestConfiguration", proxyBeanMethods = false)
    static class AuditTestConfiguration implements AuditTrailExecutionPlanConfigurer {
        @Override
        public void configureAuditTrailExecutionPlan(final AuditTrailExecutionPlan plan) {
            plan.registerAuditTrailManager(new MockAuditTrailManager());
        }
    }
    
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasReportsAutoConfiguration.class,
        CasCoreLoggingAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasWebAppAutoConfiguration.class,
        CasCoreValidationAutoConfiguration.class,
        CasValidationAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasThemesAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import({
        AbstractCasEndpointTests.AuditTestConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasRegisteredServicesTestConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
