package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreEventsConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasEventsInMemoryRepositoryConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.config.ElectronicFenceConfiguration;
import org.apereo.cas.config.GoogleMapsGeoCodingConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.mock.MockTicketGrantingTicketCreatedEventProducer;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.events.CasEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.net.ssl.HttpsURLConnection;

/**
 * This is {@link BaseAuthenticationRequestRiskCalculatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseAuthenticationRequestRiskCalculatorTests.SharedTestConfiguration.class)
@EnableScheduling
public abstract class BaseAuthenticationRequestRiskCalculatorTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    protected CasEventRepository casEventRepository;

    @Autowired
    @Qualifier("authenticationRiskEvaluator")
    protected AuthenticationRiskEvaluator authenticationRiskEvaluator;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationRiskEmailNotifier")
    protected AuthenticationRiskNotifier authenticationRiskEmailNotifier;

    @Autowired
    @Qualifier("authenticationRiskSmsNotifier")
    protected AuthenticationRiskNotifier authenticationRiskSmsNotifier;

    @BeforeEach
    public void prepTest() throws Throwable {
        MockTicketGrantingTicketCreatedEventProducer.createEvents(this.casEventRepository);
        HttpsURLConnection.setDefaultHostnameVerifier(CasSSLContext.disabled().getHostnameVerifier());
        HttpsURLConnection.setDefaultSSLSocketFactory(CasSSLContext.disabled().getSslContext().getSocketFactory());
    }

    @TestConfiguration(value = "ElectronicFenceTestConfiguration", proxyBeanMethods = false)
    static class ElectronicFenceTestConfiguration {
        @Bean
        public SmsSender smsSender() {
            return MockSmsSender.INSTANCE;
        }
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        ElectronicFenceTestConfiguration.class,
        ElectronicFenceConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasCoreServicesConfiguration.class,
        GoogleMapsGeoCodingConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasEventsInMemoryRepositoryConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreEventsConfiguration.class
    })
    static class SharedTestConfiguration {
    }
}
