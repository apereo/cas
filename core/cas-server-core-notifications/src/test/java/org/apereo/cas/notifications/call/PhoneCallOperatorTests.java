package org.apereo.cas.notifications.call;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PhoneCallOperatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    PhoneCallOperatorTests.PhoneCallOperatorTestConfiguration.class,
    CasCoreNotificationsConfiguration.class
})
@Tag("SMS")
public class PhoneCallOperatorTests {
    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    @Test
    void verifyMailSender() throws Throwable {
        assertTrue(communicationsManager.isPhoneOperatorDefined());
        var callRequest = PhoneCallRequest.builder().to("+1234567890").from("+1234567890").text("Hello!").build();
        assertTrue(communicationsManager.phoneCall(callRequest));

        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("casuser");
        when(principal.getAttributes()).thenReturn(CollectionUtils.wrap("phone", List.of("+1234567890")));
        callRequest = PhoneCallRequest.builder().from("+1234567890").text("Hello!").principal(principal).attribute("phone").build();
        assertTrue(communicationsManager.phoneCall(callRequest));

        callRequest = PhoneCallRequest.builder().from("+1234567890").build();
        assertFalse(communicationsManager.phoneCall(callRequest));
    }

    @TestConfiguration(value = "PhoneCallOperatorTestConfiguration", proxyBeanMethods = false)
    public static class PhoneCallOperatorTestConfiguration {
        @Bean
        public PhoneCallOperator phoneCallOperator() {
            return new PhoneCallOperator() {
                @Override
                public boolean call(final String from, final String to, final String message) {
                    return true;
                }
            };
        }
    }
}
