package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
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
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    SurrogateAuthenticationEventListenerTests.SurrogateAuthenticationEventListenerTestConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
},
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "cas.authn.surrogate.sms.text=Message",
        "cas.authn.surrogate.sms.from=3487244312"
    })
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
class SurrogateAuthenticationEventListenerTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    private ClientInfo clientInfo;

    @BeforeEach
    public void setup(){
        clientInfo = ClientInfoHolder.getClientInfo();
    }

    @Test
    void verifyOperation() throws Throwable {
        val listener = new DefaultSurrogateAuthenticationEventListener(communicationsManager, casProperties);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("phone", List.of("1234567890"), "mail", List.of("cas@example.org")));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                listener.handleSurrogateAuthenticationFailureEvent(new CasSurrogateAuthenticationFailureEvent(this,
                    principal, "surrogate", clientInfo));
                listener.handleSurrogateAuthenticationSuccessEvent(new CasSurrogateAuthenticationSuccessfulEvent(this,
                    principal, "surrogate", clientInfo));
            }
        });
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val listener = new DefaultSurrogateAuthenticationEventListener(communicationsManager, casProperties);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                listener.handleSurrogateAuthenticationFailureEvent(new CasSurrogateAuthenticationFailureEvent(this,
                    principal, "surrogate", clientInfo));
                listener.handleSurrogateAuthenticationSuccessEvent(new CasSurrogateAuthenticationSuccessfulEvent(this,
                    principal, "surrogate", clientInfo));
            }
        });
    }

    @TestConfiguration(value = "SurrogateAuthenticationEventListenerTestConfiguration", proxyBeanMethods = false)
        static class SurrogateAuthenticationEventListenerTestConfiguration {

        @Bean
        public SmsSender smsSender() {
            return MockSmsSender.INSTANCE;
        }
    }
}
