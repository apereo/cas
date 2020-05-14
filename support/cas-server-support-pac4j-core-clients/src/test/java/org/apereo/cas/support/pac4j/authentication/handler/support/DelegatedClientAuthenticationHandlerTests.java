package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.JEESessionStore;
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

import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DelegatedClientAuthenticationHandler}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("Simple")
public class DelegatedClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://localhost:8080/callback";
    private static final String ID = "123456789";

    private FacebookClient fbClient;
    private DelegatedClientAuthenticationHandler handler;
    private ClientCredential clientCredential;

    @BeforeEach
    public void initialize() {
        this.fbClient = new FacebookClient();
        val clients = new Clients(CALLBACK_URL, fbClient);
        this.handler = new DelegatedClientAuthenticationHandler(StringUtils.EMPTY, 0,
            mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(), clients,
            DelegatedClientUserProfileProvisioner.noOp(), new JEESessionStore());
        this.handler.setTypedIdUsed(true);

        val credentials = new OAuth20Credentials(null);
        this.clientCredential = new ClientCredential(credentials, fbClient.getName());
        val mock = new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        ExternalContextHolder.setExternalContext(mock);
    }

    @Test
    public void verifyOk() throws GeneralSecurityException, PreventedException {
        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> Optional.of(facebookProfile));
        val result = this.handler.authenticate(this.clientCredential);
        val principal = result.getPrincipal();
        assertEquals(FacebookProfile.class.getName() + '#' + ID, principal.getId());
    }

    @Test
    public void verifyOkWithSimpleIdentifier() throws GeneralSecurityException, PreventedException {
        this.handler.setTypedIdUsed(false);

        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> Optional.of(facebookProfile));
        val result = this.handler.authenticate(this.clientCredential);
        val principal = result.getPrincipal();
        assertEquals(ID, principal.getId());
    }

    @Test
    public void verifyNoProfile() {
        assertThrows(PreventedException.class, () -> {
            this.fbClient.setProfileCreator((oAuth20Credentials, webContext) -> Optional.empty());
            this.handler.authenticate(this.clientCredential);
        });
    }
}
