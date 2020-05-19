package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.MockSmsSender;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.util.io.SmsSender;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

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
    CasCoreUtilConfiguration.class,
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
@EnabledIfPortOpen(port = 25000)
public class SurrogateAuthenticationEventListenerTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Test
    public void verifyOperation() {
        val listener = new SurrogateAuthenticationEventListener(communicationsManager, casProperties);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("phone", List.of("1234567890"), "mail", List.of("cas@example.org")));

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                listener.handleSurrogateAuthenticationFailureEvent(new CasSurrogateAuthenticationFailureEvent(this,
                    principal, "surrogate"));
                listener.handleSurrogateAuthenticationSuccessEvent(new CasSurrogateAuthenticationSuccessfulEvent(this,
                    principal, "surrogate"));
            }
        });
    }

    @TestConfiguration("SurrogateAuthenticationEventListenerTestConfiguration")
    @Lazy(false)
    public static class SurrogateAuthenticationEventListenerTestConfiguration {

        @Bean
        public SmsSender smsSender() {
            return new MockSmsSender();
        }
    }
}
