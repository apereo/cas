package org.apereo.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.support.oauth.validator.OAuthValidator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthWebApplicationService;
import org.apereo.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.mvc.AbstractController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * It finds the right sub controller to call according to the url.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public abstract class BaseOAuthWrapperController extends AbstractController {

    /** The logger. */
    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    /** The services manager. */
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /** The ticket registry. */
    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    /** The access token timeout. */
    
    @Value("${tgt.timeToKillInSeconds:7200}")
    protected long timeout;

    /** The OAuth validator. */
    @Autowired
    @Qualifier("oAuthValidator")
    protected OAuthValidator validator;

    /** The JSON factory. */
    protected final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    
    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory accessTokenFactory;

    
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
        final AccessToken accessToken = this.accessTokenFactory.create(service, authentication);
        this.ticketRegistry.addTicket(accessToken);
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
     * @param service the registered service
     * @return the built authentication
     */
    protected Authentication createAuthentication(final UserProfile profile, final RegisteredService service) {
        final Principal principal = this.principalFactory.createPrincipal(profile.getId(), profile.getAttributes());

        final Map<String, Object> newAttributes = service.getAttributeReleasePolicy().getAttributes(principal);
        final Principal newPrincipal = principalFactory.createPrincipal(profile.getId(), newAttributes);

        final String authenticator = profile.getClass().getCanonicalName();
        final CredentialMetaData metadata = new BasicCredentialMetaData(
                new BasicIdentifiableCredential(profile.getId()));
        final HandlerResult handlerResult = new DefaultHandlerResult(authenticator, metadata, newPrincipal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
                .addAttribute("permissions", profile.getPermissions())
                .addAttribute("roles", profile.getRoles())
                .addCredential(metadata)
                .setPrincipal(newPrincipal)
                .setAuthenticationDate(ZonedDateTime.now())
                .addSuccess(profile.getClass().getCanonicalName(), handlerResult)
                .build();
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public TicketRegistry getTicketRegistry() {
        return this.ticketRegistry;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public AccessTokenFactory getAccessTokenFactory() {
        return this.accessTokenFactory;
    }

    public void setAccessTokenFactory(final AccessTokenFactory accessTokenFactory) {
        this.accessTokenFactory = accessTokenFactory;
    }

    public OAuthValidator getValidator() {
        return this.validator;
    }

    public void setValidator(final OAuthValidator validator) {
        this.validator = validator;
    }

    public PrincipalFactory getPrincipalFactory() {
        return this.principalFactory;
    }

    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
