package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.BasicIdentifiableCredential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.services.OAuthWebApplicationService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessTokenFactory;
import org.jasig.cas.support.oauth.validator.OAuthValidator;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * It finds the right sub controller to call according to the url.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public abstract class BaseOAuthWrapperController extends AbstractController {

    /** The logger. */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /** The ticket registry. */
    @NotNull
    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    /** The access token timeout. */
    @NotNull
    @Value("${tgt.timeToKillInSeconds:7200}")
    protected long timeout;

    /** The OAuth validator. */
    @NotNull
    @Autowired
    @Qualifier("oAuthValidator")
    protected OAuthValidator validator;

    /** The JSON factory. */
    protected final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @NotNull
    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory accessTokenFactory;

    @NotNull
    @Autowired
    @Qualifier("defaultPrincipalFactory")
    private PrincipalFactory principalFactory;

    /**
     * Generate an access token from a service and authentication.
     *
     * @param service the service
     * @param authentication the authentication
     * @return an access token
     */
    protected AccessToken generateAccessToken(final Service service, final Authentication authentication) {
        final AccessToken accessToken = accessTokenFactory.create(service, authentication);
        ticketRegistry.addTicket(accessToken);
        return accessToken;
    }

    /**
     * Create an OAuth service from a registered service.
     *
     * @param registeredService the registered service
     * @return the OAuth service
     */
    protected OAuthWebApplicationService createService(final RegisteredService registeredService) {
        return new OAuthWebApplicationService(registeredService);
    }

    /**
     * Create an authentication from a user profile.
     *
     * @param profile the given user profile
     * @return the built authentication
     */
    protected Authentication createAuthentication(final UserProfile profile) {
        final Principal principal = principalFactory.createPrincipal(profile.getId(), profile.getAttributes());
        final String authenticator = profile.getClass().getCanonicalName();
        final CredentialMetaData metadata = new BasicCredentialMetaData(
                new BasicIdentifiableCredential(profile.getId()));
        final HandlerResult handlerResult = new DefaultHandlerResult(authenticator, metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
                .addAttribute("permissions", profile.getPermissions())
                .addAttribute("roles", profile.getRoles())
                .addCredential(metadata)
                .setPrincipal(principal)
                .setAuthenticationDate(ZonedDateTime.now())
                .addSuccess(profile.getClass().getCanonicalName(), handlerResult)
                .build();
        
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public AccessTokenFactory getAccessTokenFactory() {
        return accessTokenFactory;
    }

    public void setAccessTokenFactory(final AccessTokenFactory accessTokenFactory) {
        this.accessTokenFactory = accessTokenFactory;
    }

    public OAuthValidator getValidator() {
        return validator;
    }

    public void setValidator(final OAuthValidator validator) {
        this.validator = validator;
    }

    public PrincipalFactory getPrincipalFactory() {
        return principalFactory;
    }

    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
