package org.apereo.cas.support.pac4j.authentication.handler.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.services.ServicesManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.pac4j.core.client.Clients;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.facebook.FacebookProfile;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ClientAuthenticationHandler}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@Slf4j
public class ClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://localhost:8080/callback";
    private static final String ID = "123456789";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FacebookClient fbClient;
    private ClientAuthenticationHandler handler;
    private ClientCredential clientCredential;

    @Before
    public void initialize() {
        this.fbClient = new FacebookClient();
        final var clients = new Clients(CALLBACK_URL, fbClient);
        this.handler = new ClientAuthenticationHandler("", mock(ServicesManager.class), null, clients);
        this.handler.setTypedIdUsed(true);

        final Credentials credentials = new OAuth20Credentials(null);
        this.clientCredential = new ClientCredential(credentials, fbClient.getName());
        final var mock = new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        ExternalContextHolder.setExternalContext(mock);
    }

    @Test
    public void verifyOk() throws GeneralSecurityException, PreventedException {
        final var facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> facebookProfile);
        final var result = this.handler.authenticate(this.clientCredential);
        final var principal = result.getPrincipal();
        assertEquals(FacebookProfile.class.getName() + '#' + ID, principal.getId());
    }

    @Test
    public void verifyOkWithSimpleIdentifier() throws GeneralSecurityException, PreventedException {
        this.handler.setTypedIdUsed(false);

        final var facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> facebookProfile);
        final var result = this.handler.authenticate(this.clientCredential);
        final var principal = result.getPrincipal();
        assertEquals(ID, principal.getId());
    }

    @Test
    public void verifyNoProfile() throws GeneralSecurityException, PreventedException {
        this.thrown.expect(FailedLoginException.class);
        this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> null);
        this.handler.authenticate(this.clientCredential);
    }
}
