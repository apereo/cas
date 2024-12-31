package org.apereo.cas.support.pac4j.authentication.handler;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationCoreProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.clients.RefreshableDelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandler;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
import java.util.List;
import java.util.Optional;

/**
 * Tests the {@link DelegatedClientAuthenticationHandler}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@Tag("AuthenticationHandler")
class DelegatedClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://localhost:8080/callback";

    private static final String ID = "123456789";

    private FacebookClient fbClient;

    private DelegatedClientAuthenticationHandler handler;

    private ClientCredential clientCredential;

    @BeforeEach
    void initialize() throws Throwable {
        val ctx = new StaticApplicationContext();
        ctx.refresh();

        ApplicationContextProvider.holdApplicationContext(ctx);
        val processor = Mockito.mock(DelegatedAuthenticationPreProcessor.class);
        Mockito.when(processor.process(ArgumentMatchers.any(Principal.class), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Principal.class));
        ApplicationContextProvider.registerBeanIntoApplicationContext(ctx,
            processor, "customDelegatedAuthenticationPreProcessor");

        fbClient = new FacebookClient();
        val clients = new RefreshableDelegatedIdentityProviders(CALLBACK_URL, DelegatedIdentityProviderFactory.withClients(List.of(fbClient)));
        
        handler = new DelegatedClientAuthenticationHandler(new Pac4jDelegatedAuthenticationCoreProperties(),
            Mockito.mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory(), clients,
            DelegatedClientUserProfileProvisioner.noOp(), new JEESessionStore(), ctx);
        handler.setTypedIdUsed(true);

        val credentials = new OAuth20Credentials(null);
        clientCredential = new ClientCredential(credentials, fbClient.getName());
        val mock = new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        ExternalContextHolder.setExternalContext(mock);
    }

    @Test
    void verifyOk() throws Throwable {
        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));
        val result = handler.authenticate(clientCredential, Mockito.mock(Service.class));
        val principal = result.getPrincipal();
        Assertions.assertEquals(FacebookProfile.class.getName() + '#' + ID, principal.getId());
    }

    @Test
    void verifyMissingClient() {
        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));

        val cc = new ClientCredential(new AnonymousCredentials(), "UnknownClient");
        Assertions.assertThrows(PreventedException.class, () -> handler.authenticate(cc, Mockito.mock(Service.class)));
    }

    @Test
    void verifyOkWithSimpleIdentifier() throws Throwable {
        handler.setTypedIdUsed(false);

        val facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        fbClient.setProfileCreator((callContext, sessionStore) -> Optional.of(facebookProfile));
        val result = handler.authenticate(clientCredential, Mockito.mock(Service.class));
        val principal = result.getPrincipal();
        Assertions.assertEquals(ID, principal.getId());
    }

    @Test
    void verifyNoProfile() {
        Assertions.assertThrows(PreventedException.class, () -> {
            fbClient.setProfileCreator((callContext, sessionStore) -> Optional.empty());
            handler.authenticate(clientCredential, Mockito.mock(Service.class));
        });
    }

}
