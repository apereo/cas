package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationAction;
import org.apereo.cas.web.flow.CasWebflowConstants;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.webflow.context.ExternalContextHolder.setExternalContext;
import static org.springframework.webflow.execution.RequestContextHolder.setRequestContext;

/**
 * This is {@link GoogleAuthenticatorSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorSaveRegistrationActionTests.GoogleAuthenticatorSaveRegistrationActionTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
})
@Tag("WebflowMfaActions")
public class GoogleAuthenticatorSaveRegistrationActionTests {

    @Autowired
    @Qualifier("googleSaveAccountRegistrationAction")
    private Action googleSaveAccountRegistrationAction;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @BeforeEach
    public void beforeEach() {
        googleAuthenticatorAccountRegistry.deleteAll();
    }

    @Test
    public void verifyMultipleRegDisabled(@Autowired final CasConfigurationProperties casProperties) throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        val acct = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);
        casProperties.getAuthn().getMfa().getGauth().getCore().setMultipleDeviceRegistrationEnabled(false);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
    }

    @Test
    public void verifyAccountValidationFails() throws Exception {
        val acct = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "918273");
        request.addParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, acct.getName());
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());

        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
    }

    @Test
    public void verifyAccountValidationOnly() throws Exception {
        val acct = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "123456");
        request.addParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, acct.getName());
        request.addParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "true");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());
        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);

        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "987654");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "112233");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        
        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "123456");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, googleSaveAccountRegistrationAction.execute(context).getId());
    }

    @TestConfiguration("GoogleAuthenticatorSaveRegistrationActionTests")
    public static class GoogleAuthenticatorSaveRegistrationActionTestConfiguration {
        @Bean
        public IGoogleAuthenticator googleAuthenticatorInstance() {
            val auth = mock(IGoogleAuthenticator.class);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(123456))).thenReturn(Boolean.TRUE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(987654))).thenReturn(Boolean.FALSE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(112233))).thenThrow(new IllegalArgumentException());
            return auth;
        }
    }
}
