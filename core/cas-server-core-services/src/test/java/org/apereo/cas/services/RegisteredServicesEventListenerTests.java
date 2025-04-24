package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicesEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    RegisteredServicesEventListenerTests.RegisteredServicesEventListenerTestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class
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
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RegisteredServicesEventListenerTests {
    
    @Autowired
    @Qualifier("registeredServicesEventListener")
    private RegisteredServicesEventListener registeredServicesEventListener;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    private ClientInfo clientInfo;

    @BeforeEach
    void setup(){
        clientInfo = ClientInfoHolder.getClientInfo();
    }

    @Test
    void verifyServiceExpirationEventNoContact() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                val event = new CasRegisteredServiceExpiredEvent(this, registeredService, false, clientInfo);
                registeredServicesEventListener.handleRegisteredServiceExpiredEvent(event);
            }
        });
    }

    @Test
    void verifyServiceExpirationEventWithContact() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, false, clientInfo);
        assertDoesNotThrow(() -> registeredServicesEventListener.handleRegisteredServiceExpiredEvent(event));
    }

    @Test
    void verifyServiceExpirationWithRemovalEvent() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val contact = new DefaultRegisteredServiceContact();
        contact.setName("Test");
        contact.setEmail("casuser@example.org");
        contact.setPhone("13477465421");
        registeredService.getContacts().add(contact);
        registeredServicesEventListener.handleRefreshEvent(new CasRegisteredServicesRefreshEvent(this, clientInfo));
        registeredServicesEventListener.handleEnvironmentChangeEvent(new EnvironmentChangeEvent(Set.of()));
        registeredServicesEventListener.handleContextRefreshedEvent(new ContextRefreshedEvent(applicationContext));
        val event = new CasRegisteredServiceExpiredEvent(this, registeredService, true, clientInfo);
        registeredServicesEventListener.handleRegisteredServiceExpiredEvent(event);
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
