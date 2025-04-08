package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationCoreProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Authentication handler which gets the credentials and then the user profile
 * in a delegated authentication process from an external identity provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@Monitorable
public class DelegatedClientAuthenticationHandler extends BaseDelegatedClientAuthenticationHandler {

    private final DelegatedIdentityProviders identityProviders;

    private final DelegatedClientUserProfileProvisioner profileProvisioner;

    private final ConfigurableApplicationContext applicationContext;

    public DelegatedClientAuthenticationHandler(final Pac4jDelegatedAuthenticationCoreProperties properties,
                                                final ServicesManager servicesManager,
                                                final PrincipalFactory principalFactory,
                                                final DelegatedIdentityProviders identityProviders,
                                                final DelegatedClientUserProfileProvisioner profileProvisioner,
                                                final SessionStore sessionStore,
                                                final ConfigurableApplicationContext applicationContext) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder(), sessionStore);
        this.identityProviders = identityProviders;
        this.profileProvisioner = profileProvisioner;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && ClientCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential,
                                                                    final Service service) throws PreventedException {
        return FunctionUtils.doAndHandle(() -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext();
            val webContext = new JEEContext(Objects.requireNonNull(request),
                Objects.requireNonNull(response));
            val clientCredentials = (ClientCredential) credential;
            LOGGER.debug("Located client credentials as [{}]", clientCredentials);

            LOGGER.trace("Client name: [{}]", clientCredentials.getClientName());
            val client = identityProviders.findClient(clientCredentials.getClientName(), webContext)
                .map(BaseClient.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unable to determine client based on client name "
                    + clientCredentials.getClientName()));
            LOGGER.trace("Delegated client is: [{}]", client);

            var userProfileResult = Optional.ofNullable(clientCredentials.getUserProfile());
            if (userProfileResult.isEmpty()) {
                val credentials = clientCredentials.getCredentials();

                val callContext = new CallContext(webContext, this.sessionStore);
                userProfileResult = client.getUserProfile(callContext, credentials);
            }
            val userProfile = userProfileResult.orElseThrow(
                () -> new PreventedException("Unable to fetch user profile from client " + client.getName()));
            LOGGER.debug("Final user profile is: [{}]", userProfile);
            userProfile.setClientName(clientCredentials.getClientName());
            storeUserProfile(webContext, userProfile);
            return createResult(clientCredentials, userProfile, client, service);
        }, e -> {
            throw new PreventedException(e);
        }).get();
    }

    @Override
    protected void preFinalizeAuthenticationHandlerResult(final ClientCredential credentials, final Principal principal,
                                                          final UserProfile profile, final BaseClient client, final Service service) throws Throwable {
        profileProvisioner.execute(principal, profile, client, credentials);
    }

    @Override
    protected Principal finalizeAuthenticationPrincipal(final Principal initialPrincipal, final BaseClient client,
                                                        final ClientCredential credential, final Service service) throws Throwable {
        val processors = applicationContext.getBeansOfType(DelegatedAuthenticationPreProcessor.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());
        AnnotationAwareOrderComparator.sortIfNecessary(processors);
        var processingPrincipal = initialPrincipal;
        for (val processor : processors) {
            processingPrincipal = processor.process(processingPrincipal, client, credential, service);
        }
        return processingPrincipal;
    }
}
