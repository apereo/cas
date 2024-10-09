package org.apereo.cas.web.flow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.MockRequestContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.webflow.execution.Action;

@Tag("WebflowAuthenticationActions")
@Import(CreatePasswordlessAuthenticationTokenActionTest.PasswordlessAuthenticationActionTestConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.passwordless.tokens.sms.text=${token}"
})
class CreatePasswordlessAuthenticationTokenActionTest extends BasePasswordlessAuthenticationActionTests {
    @TestConfiguration(
        value = "PasswordlessAuthenticationActionTestConfiguration",
        proxyBeanMethods = false)
    static class PasswordlessAuthenticationActionTestConfiguration {
        @Bean
        @Qualifier(SmsSender.BEAN_NAME)
        public SmsSender smsSender() {
          return MockSmsSender.INSTANCE;
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class MockSmsSender implements SmsSender {
            static final SmsSender INSTANCE = new MockSmsSender();

            @Override
            public boolean canSend() {
                return true;
            }

            @Override
            public boolean send(final String from, final String to, final String message) {
                return true;
            }
        }
    }

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN)
    private Action createPasswordlessAuthenticationTokenAction;

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    @MockitoSpyBean
    private CommunicationsManager communicationsManager;

    @Test
    void verifySms() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val user = PasswordlessUserAccount.builder().username("casuser").phone("+15555555555").build();
        var token = createToken(user);
        ArgumentCaptor<SmsRequest> argument = ArgumentCaptor.forClass(SmsRequest.class);

        ((CreatePasswordlessAuthenticationTokenAction) createPasswordlessAuthenticationTokenAction)
            .smsToken(context, user, token);

        verify(communicationsManager).sms(argument.capture());
        assertEquals(token.getToken(), argument.getValue().getText());
    }

    private PasswordlessAuthenticationToken createToken(PasswordlessUserAccount user) {
        val passwordlessRequest =
            PasswordlessAuthenticationRequest.builder().username(user.getUsername()).build();
        val token = passwordlessTokenRepository.createToken(user, passwordlessRequest);
        passwordlessTokenRepository.saveToken(user, passwordlessRequest, token);
        return token;
    }
}
