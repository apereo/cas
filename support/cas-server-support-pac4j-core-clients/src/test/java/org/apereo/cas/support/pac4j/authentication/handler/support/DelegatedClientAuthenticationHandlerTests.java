package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.facebook.FacebookProfile;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DelegatedClientAuthenticationHandler}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@Tag("AuthenticationHandler")
public class DelegatedClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://localhost:8080/callback";

    private static final String ID = "123456789";

    private FacebookClient fbClient;

    private DelegatedClientAuthenticationHandler handler;

    private ClientCredential clientCredential;

    @BeforeEach
    public void initialize() {
        val ctx = new StaticApplicationContext();
        ctx.refresh();

        ApplicationContextProvider.holdApplicationContext(ctx);
        val processor = mock(DelegatedAuthenticationPreProcessor.class);
        when(processor.process(any(Principal.class), any(), any(), any()))
            .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Principal.class));
        ApplicationContextProvider.registerBeanIntoApplicationContext(ctx,
            processor, "customDelegatedAuthenticationPreProcessor");

        fbClient = new FacebookClient();
        val clients = new Clients(CALLBACK_URL, fbClient);
        handler = new DelegatedClientAuthenticationHandler(new Pac4jDelegatedAuthenticationCoreProperties(),
            mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(), clients,
            DelegatedClientUserProfileProvisioner.noOp(), JEESessionStore.INSTANCE, ctx);
        handler.setTypedIdUsed(true);

        val credentials = new OAuth20Credentials(null);
        clientCredential = new ClientCredential(credentials, fbClient.getName());
        val mock = new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        ExternalContextHolder.setExternalContext(mock);
    }

    @Test
    public void verifyOk() throws Exception {
        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));
        val result = handler.authenticate(clientCredential, mock(Service.class));
        val principal = result.getPrincipal();
        assertEquals(FacebookProfile.class.getName() + '#' + ID, principal.getId());
    }

    @Test
    public void verifyMissingClient() {
        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));

        val cc = new ClientCredential(new AnonymousCredentials(), "UnknownClient");
        assertThrows(PreventedException.class, () -> handler.authenticate(cc, mock(Service.class)));
    }

    @Test
    public void verifyOkWithSimpleIdentifier() throws Exception {
        handler.setTypedIdUsed(false);

        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));
        val result = handler.authenticate(clientCredential, mock(Service.class));
        val principal = result.getPrincipal();
        assertEquals(ID, principal.getId());
    }

    @Test
    public void verifyNoProfile() {
        assertThrows(PreventedException.class, () -> {
            fbClient.setProfileCreator((callContext, sessionStore) -> Optional.empty());
            handler.authenticate(clientCredential, mock(Service.class));
        });
    }

}
