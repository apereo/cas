package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Bean;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicesEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    RegisteredServicesEventListenerTests.RegisteredServicesEventListenerTestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
}, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "cas.service-registry.sms.text=Service ${service} has expired in CAS service registry",
    "cas.service-registry.sms.from=3477563421",
    "cas.service-registry.mail.from=admin@example.org",
    "cas.service-registry.mail.subject=Sample Subject",
    "cas.service-registry.mail.text=Service ${service} has expired in CAS service registry"
})
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RegisteredServicesEventListenerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    private ClientInfo clientInfo;

    @BeforeEach
    public void setup(){
        clientInfo = ClientInfoHolder.getClientInfo();
    }

    @Test
    void verifyServiceExpirationEventNoContact() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val listener = new DefaultRegisteredServicesEventListener(servicesManager, casProperties, communicationsManager);
                val event = new CasRegisteredServiceExpiredEvent(this, registeredService, false, clientInfo);
                listener.handleRegisteredServiceExpiredEvent(event);
            }
        });
    }

    @Test
    void verifyServiceExpirationEventWithContact() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        val listener = new DefaultRegisteredServicesEventListener(this.servicesManager, casProperties, communicationsManager);
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, false, clientInfo);
        assertDoesNotThrow(() -> listener.handleRegisteredServiceExpiredEvent(event));
    }

    @Test
    void verifyServiceExpirationWithRemovalEvent() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        val listener = new DefaultRegisteredServicesEventListener(this.servicesManager, casProperties, communicationsManager);
        listener.handleRefreshEvent(new CasRegisteredServicesRefreshEvent(this, clientInfo));
        listener.handleEnvironmentChangeEvent(new EnvironmentChangeEvent(Set.of()));
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, true, clientInfo);
        listener.handleRegisteredServiceExpiredEvent(event);
    }

    @TestConfiguration(value = "RegisteredServicesEventListenerTestConfiguration", proxyBeanMethods = false)
    static class RegisteredServicesEventListenerTestConfiguration {

        @ConditionalOnMissingBean(name = SmsSender.BEAN_NAME)
        @Bean
        public SmsSender smsSender() {
            return MockSmsSender.INSTANCE;
        }
    }
}
