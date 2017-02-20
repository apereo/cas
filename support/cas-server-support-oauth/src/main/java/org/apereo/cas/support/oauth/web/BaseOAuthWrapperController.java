package org.apereo.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * It finds the right sub controller to call according to the url.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Controller
public abstract class BaseOAuthWrapperController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOAuthWrapperController.class);

    /**
     * Collection of CAS settings.
     **/
    protected final CasConfigurationProperties casProperties;

    /**
     * Convert profile scopes to attributes.
     */
    protected final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;
    private final OAuth20Validator validator;
    private final AccessTokenFactory accessTokenFactory;
    private final PrincipalFactory principalFactory;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    public BaseOAuthWrapperController(final ServicesManager servicesManager,
                                      final TicketRegistry ticketRegistry,
                                      final OAuth20Validator validator,
                                      final AccessTokenFactory accessTokenFactory,
                                      final PrincipalFactory principalFactory,
                                      final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                      final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                      final CasConfigurationProperties casProperties) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.validator = validator;
        this.accessTokenFactory = accessTokenFactory;
        this.principalFactory = principalFactory;
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
        this.casProperties = casProperties;
        this.scopeToAttributesFilter = scopeToAttributesFilter;
    }

    /**
     * Generate an access token from a service and authentication.
     *
     * @param service        the service
     * @param authentication the authentication
     * @param context        the context
     * @return an access token
     */
    protected AccessToken generateAccessToken(final Service service,
                                              final Authentication authentication,
                                              final J2EContext context) {
        final AccessToken accessToken = this.accessTokenFactory.create(service, authentication);
        LOGGER.debug("Creating access token [{}]", accessToken);
        this.ticketRegistry.addTicket(accessToken);
        LOGGER.debug("Added access token [{}] to registry", accessToken);
        return accessToken;
    }

    /**
     * Create an OAuth service from a registered service.
     *
     * @param registeredService the registered service
     * @param context           the context
     * @return the OAuth service
     */
    protected WebApplicationService createService(final RegisteredService registeredService,
                                                  final J2EContext context) {
        return webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
    }

    /**
     * Create an authentication from a user profile.
     *
     * @param profile           the given user profile
     * @param registeredService the registered service
     * @param context           the context
     * @param service           the service
     * @return the built authentication
     */
    protected Authentication createAuthentication(final UserProfile profile,
                                                  final RegisteredService registeredService,
                                                  final J2EContext context,
                                                  final Service service) {
        final Principal newPrincipal =
                this.scopeToAttributesFilter.filter(service,
                        this.principalFactory.createPrincipal(profile.getId(), profile.getAttributes()),
                        registeredService,
                        context);

        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        final String authenticator = profile.getClass().getCanonicalName();
        final CredentialMetaData metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(profile.getId()));
        final HandlerResult handlerResult = new DefaultHandlerResult(authenticator, metadata, newPrincipal, new ArrayList<>());

        final String state = StringUtils.defaultIfBlank(context.getRequestParameter(OAuthConstants.STATE), StringUtils.EMPTY);
        final String nonce = StringUtils.defaultIfBlank(context.getRequestParameter(OAuthConstants.NONCE), StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuthConstants.STATE, state, OAuthConstants.NONCE, nonce);

        final AuthenticationBuilder bldr = DefaultAuthenticationBuilder.newInstance()
                .addAttribute("permissions", profile.getPermissions())
                .addAttribute("roles", profile.getRoles())
                .addAttribute(OAuthConstants.STATE, state)
                .addAttribute(OAuthConstants.NONCE, nonce)
                .addCredential(metadata)
                .setPrincipal(newPrincipal)
                .setAuthenticationDate(ZonedDateTime.now())
                .addSuccess(profile.getClass().getCanonicalName(), handlerResult);

        // Add "other" profile attributes as authentication attributes.
        if (casProperties.getAuthn().getOauth().getAccessToken().isReleaseProtocolAttributes()) {
            profile.getAttributes().forEach((k, v) -> {
                if (!newPrincipal.getAttributes().containsKey(k)) {
                    LOGGER.debug("Added attribute [{}] with value [{}] to the authentication", k, v);
                    bldr.addAttribute(k, v);
                } else {
                    LOGGER.debug("Skipped over attribute [{}] since it's already contained by the principal", k);
                }
            });
        }
        return bldr.build();
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public OAuth20Validator getValidator() {
        return validator;
    }
}
