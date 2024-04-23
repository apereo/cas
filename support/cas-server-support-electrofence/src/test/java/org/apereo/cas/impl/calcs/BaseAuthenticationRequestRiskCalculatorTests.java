package org.apereo.cas.impl.calcs;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasElectronicFenceAutoConfiguration;
import org.apereo.cas.config.CasEventsInMemoryRepositoryAutoConfiguration;
import org.apereo.cas.config.CasGoogleMapsGeoCodingAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
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
        CasElectronicFenceAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasGoogleMapsGeoCodingAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasEventsInMemoryRepositoryAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreEventsAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
