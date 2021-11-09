package org.apereo.cas.okta;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.OktaAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import com.okta.authn.sdk.AuthenticationStateHandler;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.authn.sdk.resource.User;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OktaAuthenticationStateHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    OktaAuthenticationConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    BaseOktaTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.okta.proxy-host=localhost",
        "cas.authn.okta.proxy-port=1234",
        "cas.authn.okta.proxy-username=username",
        "cas.authn.okta.proxy-password=password",
        "cas.authn.okta.organization-url=https://dev-159539.oktapreview.com"
    })
@Tag("AuthenticationHandler")
public class OktaAuthenticationStateHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("oktaAuthenticationHandler")
    private AuthenticationHandler oktaAuthenticationHandler;

    @Autowired
    @Qualifier("oktaPrincipalFactory")
    private PrincipalFactory oktaPrincipalFactory;

    @Test
    public void verifyOperation() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
            "casuser@apereo.org", "a8BuQH@6B7z");
        assertThrows(FailedLoginException.class, () -> oktaAuthenticationHandler.authenticate(c));
    }

    @Test
    public void verifySuccess() throws Exception {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
            "casuser@apereo.org", "a8BuQH@6B7z");
        val response = mock(AuthenticationResponse.class);

        val user = mock(User.class);
        when(user.getLogin()).thenReturn("casuser");
        when(user.getId()).thenReturn("casuser");
        when(response.getUser()).thenReturn(user);
        when(response.getSessionToken()).thenReturn("token");
        val client = mock(AuthenticationClient.class);
        when(client.authenticate(anyString(), any(), any(), any(AuthenticationStateHandler.class)))
            .thenAnswer(invocationOnMock -> {
                val adapter = invocationOnMock.getArgument(3, AuthenticationStateHandler.class);
                adapter.handleSuccess(response);
                return response;
            });
        val handler = new OktaAuthenticationHandler(null, servicesManager,
            oktaPrincipalFactory, casProperties.getAuthn().getOkta(), client);
        assertNotNull(handler.authenticate(c));
        assertNotNull(handler.getOktaAuthenticationClient());
        assertNotNull(handler.getProperties());
    }
}
