package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.sms.SmsRequest;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CreatePasswordlessAuthenticationTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = {
    "cas.authn.passwordless.tokens.sms.text=Your token is ${token}",
    "cas.authn.passwordless.tokens.sms.from=347746584",
    "cas.authn.passwordless.tokens.mail.text=${token}",
    "cas.authn.passwordless.tokens.mail.from=cas@example.org",
    "cas.authn.passwordless.tokens.mail.subject=Passwordless"
})
class CreatePasswordlessAuthenticationTokenActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN)
    private Action createPasswordlessAuthenticationTokenAction;

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("passwordlessPrincipalFactory")
    private PrincipalFactory passwordlessPrincipalFactory;

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
    private MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyTokenNotStoredWhenNothingIsDelivered() throws Throwable {
        val username = "casuser-" + UUID.randomUUID();
        val context = prepareContext(username);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, createPasswordlessAuthenticationTokenAction.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken(username).isEmpty());
        assertTrue(context.getMessageContext().hasErrorMessages());
    }

    @Test
    void verifyTokenStoredWhenOneChannelFails() throws Throwable {
        val communicationsManager = mock(CommunicationsManager.class);
        when(communicationsManager.isMailSenderDefined()).thenReturn(true);
        when(communicationsManager.isSmsSenderDefined()).thenReturn(true);
        when(communicationsManager.email(any())).thenThrow(new IllegalStateException("Mail server is down"));
        when(communicationsManager.sms(any(SmsRequest.class))).thenReturn(true);
        val action = new CreatePasswordlessAuthenticationTokenAction(casProperties, passwordlessTokenRepository,
            communicationsManager, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory,
            authenticationSystemSupport, tenantExtractor);

        val username = "casuser-" + UUID.randomUUID();
        val context = prepareContext(username);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken(username).isPresent());
        assertFalse(context.getMessageContext().hasErrorMessages());
        verify(communicationsManager).sms(any(SmsRequest.class));
    }

    @Test
    void verifyTokenNotReissuedAfterFailedAttempt() throws Throwable {
        val communicationsManager = mock(CommunicationsManager.class);
        when(communicationsManager.isMailSenderDefined()).thenReturn(true);
        when(communicationsManager.isSmsSenderDefined()).thenReturn(true);
        val action = new CreatePasswordlessAuthenticationTokenAction(casProperties, passwordlessTokenRepository,
            communicationsManager, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory,
            authenticationSystemSupport, tenantExtractor);

        val username = "casuser-" + UUID.randomUUID();
        val account = PasswordlessUserAccount.builder().username(username).build();
        val request = PasswordlessAuthenticationRequest.builder().username(username).build();
        val issued = passwordlessTokenRepository.saveToken(account, request, passwordlessTokenRepository.createToken(account, request));

        val context = prepareContext(username);
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertEquals(issued.getToken(), passwordlessTokenRepository.findToken(username).orElseThrow().getToken());
        verify(communicationsManager, never()).email(any());
        verify(communicationsManager, never()).sms(any(SmsRequest.class));
    }

    @Test
    void verifySmsEndsWithOriginBoundCode() throws Throwable {
        val communicationsManager = mock(CommunicationsManager.class);
        when(communicationsManager.isSmsSenderDefined()).thenReturn(true);
        when(communicationsManager.sms(any(SmsRequest.class))).thenReturn(true);
        val action = new CreatePasswordlessAuthenticationTokenAction(casProperties, passwordlessTokenRepository,
            communicationsManager, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory,
            authenticationSystemSupport, tenantExtractor);

        val username = "casuser-" + UUID.randomUUID();
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(prepareContext(username)).getId());
        val token = passwordlessTokenRepository.findToken(username).orElseThrow().getToken();
        val captor = ArgumentCaptor.forClass(SmsRequest.class);
        verify(communicationsManager).sms(captor.capture());
        val host = URI.create(casProperties.getServer().getName()).getHost();
        assertEquals("Your token is %s\n\n@%s #%s".formatted(token, host, token), captor.getValue().getText());
    }

    private MockRequestContext prepareContext(final String username) throws Exception {
        val context = MockRequestContext.create(applicationContext).withDefaultMessageContext();
        val account = PasswordlessUserAccount.builder()
            .username(username)
            .email(username + "@example.org")
            .phone("3477465840")
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(context,
            PasswordlessAuthenticationRequest.builder().username(username).build());
        return context;
    }
}
