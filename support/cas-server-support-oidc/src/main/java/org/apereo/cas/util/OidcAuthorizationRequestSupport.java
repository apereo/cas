package org.apereo.cas.util;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcAuthorizationRequestSupport}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcAuthorizationRequestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthorizationRequestSupport.class);
    
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    
    private TicketRegistrySupport ticketRegistrySupport;


    /**
     * Gets oidc prompt from authorization request.
     *
     * @param url the url
     * @return the oidc prompt from authorization request
     */
    public static Set<String> getOidcPromptFromAuthorizationRequest(final String url) {
        final URIBuilder builderContext = new URIBuilder(url);
        final Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                .stream().filter(p -> OidcConstants.PROMPT.equals(p.getName()))
                .findFirst();

        if (parameter.isPresent()) {
            return ImmutableSet.copyOf(parameter.get().getValue().split(" "));
        }
        return Collections.emptySet();
    }
    
    /**
     * Gets oidc prompt from authorization request.
     *
     * @param context the context
     * @return the oidc prompt from authorization request
     */
    public static Set<String> getOidcPromptFromAuthorizationRequest(final WebContext context) {
        return getOidcPromptFromAuthorizationRequest(context.getFullRequestURL());
    }
    
    /**
     * Gets oidc max age from authorization request.
     *
     * @param context the context
     * @return the oidc max age from authorization request
     */
    public static Optional<Long> getOidcMaxAgeFromAuthorizationRequest(final WebContext context) {
        final URIBuilder builderContext = new URIBuilder(context.getFullRequestURL());
        final Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                .stream().filter(p -> OidcConstants.MAX_AGE.equals(p.getName()))
                .findFirst();

        if (parameter.isPresent()) {
            final long maxAge = NumberUtils.toLong(parameter.get().getValue(), -1);
            return Optional.of(maxAge);
        }
        return Optional.empty();
    }

    /**
     * Is authentication profile available?.
     *
     * @param context the context
     * @return the optional user profile
     */
    public static Optional<UserProfile> isAuthenticationProfileAvailable(final WebContext context) {
        final ProfileManager manager = new ProfileManager(context);
        return manager.get(true);
    }

    /**
     * Is cas authentication available?
     *
     * @param context the context
     * @return the optional authn
     */
    public Optional<Authentication> isCasAuthenticationAvailable(final WebContext context) {
        final J2EContext j2EContext = (J2EContext) context;
        if (j2EContext != null) {
            final String tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(j2EContext.getRequest());

            if (StringUtils.isNotBlank(tgtId)) {
                final Authentication authentication = ticketRegistrySupport.getAuthenticationFrom(tgtId);
                if (authentication != null) {
                    return Optional.of(authentication);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Is cas authentication old for max age authorization request boolean.
     *
     * @param context            the context
     * @param authenticationDate the authentication date
     * @return true/false
     */
    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final ZonedDateTime authenticationDate) {
        final Optional<Long> maxAge = getOidcMaxAgeFromAuthorizationRequest(context);
        if (maxAge.isPresent() && maxAge.get() > 0) {
            final long now = ZonedDateTime.now().toEpochSecond();
            final long authTime = authenticationDate.toEpochSecond();
            final long diffInSeconds = now - authTime;
            if (diffInSeconds > maxAge.get()) {
                LOGGER.info("Authentication is too old: {} and was created {} seconds ago.",
                        authTime, diffInSeconds);
                return true;
            }
        }
        return false;
    }

    /**
     * Is cas authentication old for max age authorization request?
     *
     * @param context        the context
     * @param authentication the authentication
     * @return true/false
     */
    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final Authentication authentication) {
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authentication.getAuthenticationDate());
    }

    /**
     * Is cas authentication old for max age authorization request?
     *
     * @param context the context
     * @param profile the profile
     * @return true/false
     */
    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                       final UserProfile profile) {

        final Object authTime =
                profile.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        if (authTime == null) {
            return false;
        }
        final ZonedDateTime dt = ZonedDateTime.parse(authTime.toString());
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, dt);
    }

    /**
     * Configure client for max age authorization request.
     * Sets the CAS client to ask for renewed authentication if
     * the authn time is too old based on the requested max age.
     *
     * @param casClient      the cas client
     * @param context        the context
     * @param authentication the authentication
     */
    public void configureClientForMaxAgeAuthorizationRequest(final CasClient casClient, final WebContext context,
                                                             final Authentication authentication) {
        if (isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, authentication)) {
            casClient.setRenew(true);
        }
    }

    /**
     * Configure client for prompt login authorization request.
     *
     * @param casClient the cas client
     * @param context   the context
     */
    public static void configureClientForPromptLoginAuthorizationRequest(final CasClient casClient, final WebContext context) {
        final Set<String> prompts = getOidcPromptFromAuthorizationRequest(context);
        if (prompts.contains(OidcConstants.PROMPT_LOGIN)) {
            casClient.setRenew(true);
        }
    }

    /**
     * Configure client for prompt none authorization request.
     *
     * @param casClient the cas client
     * @param context   the context
     */
    public static void configureClientForPromptNoneAuthorizationRequest(final CasClient casClient, final WebContext context) {
        final Set<String> prompts = getOidcPromptFromAuthorizationRequest(context);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            casClient.setRenew(false);
            casClient.setGateway(true);
        }
    }

    public void setTicketGrantingTicketCookieGenerator(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setTicketRegistrySupport(final TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }
}
