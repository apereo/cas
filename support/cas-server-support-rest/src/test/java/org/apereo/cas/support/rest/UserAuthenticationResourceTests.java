package org.apereo.cas.support.rest;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.rest.authentication.DefaultRestAuthenticationService;
import org.apereo.cas.rest.factory.DefaultUserAuthenticationResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.resources.UserAuthenticationResource;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link UserAuthenticationResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("RestfulApiAuthentication")
public class UserAuthenticationResourceTests {
    private static final String TICKETS_RESOURCE_URL = "/cas/v1/users";

    @Mock
    private AuthenticationSystemSupport authenticationSupport;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Mock
    private RequestedAuthenticationContextValidator requestedContextValidator;

    @InjectMocks
    private UserAuthenticationResource userAuthenticationResource;

    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        val httpRequestCredentialFactory = new UsernamePasswordRestHttpRequestCredentialFactory() {
            @Override
            public List<Credential> fromAuthentication(final HttpServletRequest request,
                                                       final MultiValueMap<String, String> requestBody,
                                                       final Authentication authentication,
                                                       final MultifactorAuthenticationProvider provider) {
                if (provider.getId().contains("unknown")) {
                    return List.of();
                }
                return List.of(new UsernamePasswordCredential("mfa-user", "mfa-user"));
            }
        };

        val api = new DefaultRestAuthenticationService(authenticationSupport, httpRequestCredentialFactory,
            new WebApplicationServiceFactory(), multifactorTriggerSelectionStrategy, servicesManager, requestedContextValidator);
        this.userAuthenticationResource = new UserAuthenticationResource(api,
            new DefaultUserAuthenticationResourceEntityResponseFactory(),
            new GenericApplicationContext());

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.userAuthenticationResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .build();
    }

    @Test
    public void verifyAuthWithMfaFails() throws Exception {
        val builder = new DefaultAuthenticationResultBuilder().collect(CoreAuthenticationTestUtils.getAuthentication());
        when(authenticationSupport.handleInitialAuthenticationTransaction(any(), any())).thenReturn(builder);
        when(requestedContextValidator.validateAuthenticationContext(any(), any(), any(), any()))
            .thenReturn(AuthenticationContextValidationResult.builder().success(false).build());
        when(multifactorTriggerSelectionStrategy.resolve(any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider("mfa-unknown")));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("username", "casuser")
            .param("password", "Mellon"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void verifyAuthWithMfa() throws Exception {
        val builder = new DefaultAuthenticationResultBuilder().collect(CoreAuthenticationTestUtils.getAuthentication());
        val result = builder.build(new DefaultPrincipalElectionStrategy());
        when(authenticationSupport.finalizeAuthenticationTransaction(any(), anyCollection())).thenReturn(result);
        when(authenticationSupport.handleInitialAuthenticationTransaction(any(), any())).thenReturn(builder);
        when(requestedContextValidator.validateAuthenticationContext(any(), any(), any(), any()))
            .thenReturn(AuthenticationContextValidationResult.builder().success(false).build());
        when(multifactorTriggerSelectionStrategy.resolve(any(), any(), any(), any()))
            .thenReturn(Optional.of(new TestMultifactorAuthenticationProvider()));

        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("username", "casuser")
            .param("password", "Mellon"))
            .andExpect(status().isOk());
    }

    @Test
    public void verifyStatus() throws Exception {
        val builder = new DefaultAuthenticationResultBuilder().collect(CoreAuthenticationTestUtils.getAuthentication());
        val result = builder.build(new DefaultPrincipalElectionStrategy());
        when(authenticationSupport.finalizeAuthenticationTransaction(any(), anyCollection())).thenReturn(result);
        when(authenticationSupport.handleInitialAuthenticationTransaction(any(), any())).thenReturn(builder);
        when(authenticationSupport.finalizeAllAuthenticationTransactions(any(), any())).thenReturn(result);
        when(requestedContextValidator.validateAuthenticationContext(any(), any(), any(), any()))
            .thenReturn(AuthenticationContextValidationResult.builder().success(false).build());
        when(multifactorTriggerSelectionStrategy.resolve(any(), any(), any(), any())).thenReturn(Optional.empty());
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("username", "casuser")
            .param("password", "Mellon"))
            .andExpect(status().isOk());
    }

    @Test
    public void verifyStatusAuthnFails() throws Exception {
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("username", "casuser")
            .param("password", "Mellon"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void verifyBadRequest() throws Exception {
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("unknown-param", "casuser"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void verifyStatusAuthnException() throws Exception {
        val ex = new AuthenticationException(CollectionUtils.wrap("error", new FailedLoginException()));
        when(authenticationSupport.handleInitialAuthenticationTransaction(any(), any())).thenThrow(ex);
        this.mockMvc.perform(post(TICKETS_RESOURCE_URL)
            .param("username", "casuser")
            .param("password", "Mellon"))
            .andExpect(status().isUnauthorized());
    }
}
