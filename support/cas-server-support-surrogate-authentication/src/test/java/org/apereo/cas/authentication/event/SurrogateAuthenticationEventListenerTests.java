package org.apereo.cas.authentication.event;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuthenticationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    SurrogateAuthenticationEventListenerTests.SurrogateAuthenticationEventListenerTestConfiguration.class
},
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "cas.authn.surrogate.sms.text=Message",
        "cas.authn.surrogate.sms.from=3487244312"
    })
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
class SurrogateAuthenticationEventListenerTests {

    @Autowired
    @Qualifier("surrogateAuthenticationEventListener")
    private SurrogateAuthenticationEventListener surrogateAuthenticationEventListener;

    @BeforeEach
    void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("23.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("phone", List.of("1234567890"), "mail", List.of("cas@example.org")));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val clientInfo = ClientInfoHolder.getClientInfo();
                val failureEvent = new CasSurrogateAuthenticationFailureEvent(this,
                    principal, "surrogate", clientInfo);
                surrogateAuthenticationEventListener.handleSurrogateAuthenticationFailureEvent(failureEvent);
                val successfulEvent = new CasSurrogateAuthenticationSuccessfulEvent(this,
                    principal, "surrogate", clientInfo);
                surrogateAuthenticationEventListener.handleSurrogateAuthenticationSuccessEvent(successfulEvent);
            }
        });
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val clientInfo = ClientInfoHolder.getClientInfo();
                val failureEvent = new CasSurrogateAuthenticationFailureEvent(this,
                    principal, "surrogate", clientInfo);
                surrogateAuthenticationEventListener.handleSurrogateAuthenticationFailureEvent(failureEvent);
                val successfulEvent = new CasSurrogateAuthenticationSuccessfulEvent(this,
                    principal, "surrogate", clientInfo);
                surrogateAuthenticationEventListener.handleSurrogateAuthenticationSuccessEvent(successfulEvent);
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
