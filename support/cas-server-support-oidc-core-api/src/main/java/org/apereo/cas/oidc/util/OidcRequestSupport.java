package org.apereo.cas.oidc.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcRequestSupport}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcRequestSupport {
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final OidcIssuerService oidcIssuerService;

    /**
     * Gets oidc prompt from authorization request.
     *
     * @param url the url
     * @return the oidc prompt from authorization request
     */
    @SneakyThrows
    public static Set<String> getOidcPromptFromAuthorizationRequest(final @NonNull String url) {
        return new URIBuilder(url).getQueryParams().stream()
            .filter(p -> OidcConstants.PROMPT.equals(p.getName()))
            .map(param -> param.getValue().split(" "))
            .flatMap(Arrays::stream)
            .collect(Collectors.toSet());
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
    @SneakyThrows
    public static Optional<Long> getOidcMaxAgeFromAuthorizationRequest(final WebContext context) {
        val builderContext = new URIBuilder(context.getFullRequestURL());
        return builderContext.getQueryParams()
            .stream()
            .filter(p -> OidcConstants.MAX_AGE.equals(p.getName()))
            .map(p -> Optional.of(p.getValue()))
            .findFirst()
            .orElseGet(() -> context.getRequestParameter(OidcConstants.MAX_AGE))
            .map(param -> {
                val maxAge = NumberUtils.toLong(param, -1);
                return Optional.of(maxAge);
            })
            .orElse(Optional.empty());
    }

    /**
     * Is authentication profile available?.
     *
     * @param context      the context
     * @param sessionStore the session store
     * @return the optional user profile
     */
    public static Optional<UserProfile> isAuthenticationProfileAvailable(final JEEContext context, final SessionStore sessionStore) {
        val manager = new ProfileManager(context, sessionStore);
        return manager.getProfile();
    }

    /**
     * Gets redirect url with error.
     *
     * @param originalRedirectUrl the original redirect url
     * @param errorCode           the error code
     * @param webContext          the web context
     * @return the redirect url with error
     */
    @SneakyThrows
    public static String getRedirectUrlWithError(final String originalRedirectUrl, final String errorCode,
                                                 final WebContext webContext) {
        val uriBuilder = new URIBuilder(originalRedirectUrl).addParameter(OAuth20Constants.ERROR, errorCode);
        webContext.getRequestParameter(OAuth20Constants.STATE).ifPresent(st -> uriBuilder.addParameter(OAuth20Constants.STATE, st));
        return uriBuilder.build().toASCIIString();
    }

    /**
     * Remove oidc prompt from authorization request.
     *
     * @param url    the url
     * @param prompt the prompt
     * @return the string
     */
    @SneakyThrows
    public static String removeOidcPromptFromAuthorizationRequest(final String url, final String prompt) {
        val uriBuilder = new URIBuilder(url);
        val newParams = uriBuilder.getQueryParams()
            .stream()
            .filter(p -> !OidcConstants.PROMPT.equals(p.getName()) || !p.getValue().equalsIgnoreCase(prompt))
            .collect(Collectors.toList());
        return uriBuilder
            .removeQuery()
            .addParameters(newParams)
            .build()
            .toASCIIString();
    }

    /**
     * Is cas authentication old for max age authorization request boolean.
     *
     * @param context            the context
     * @param authenticationDate the authentication date
     * @return true/false
     */
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                              final ZonedDateTime authenticationDate) {
        val maxAge = getOidcMaxAgeFromAuthorizationRequest(context);
        if (maxAge.isPresent() && maxAge.get() > 0) {
            val now = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
            val authTime = authenticationDate.toEpochSecond();
            val diffInSeconds = now - authTime;
            if (diffInSeconds > maxAge.get()) {
                LOGGER.info("Authentication is too old: [{}] and was created [{}] seconds ago.",
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
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
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
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context,
                                                                              final BasicUserProfile profile) {
        var authTime = profile.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        if (authTime == null) {
            authTime = profile.getAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        }
        if (authTime == null) {
            return false;
        }
        val dt = ZonedDateTime.parse(CollectionUtils.toCollection(authTime).iterator().next().toString());
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, dt);
    }

    /**
     * Is cas authentication available and old for max age authorization request?
     *
     * @param context the context
     * @return true/false
     */
    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final WebContext context) {
        return isCasAuthenticationAvailable(context)
            .filter(a -> isCasAuthenticationOldForMaxAgeAuthorizationRequest(context, a))
            .isPresent();
    }

    /**
     * Is cas authentication available?
     *
     * @param context the context
     * @return the optional authn
     */
    public Optional<Authentication> isCasAuthenticationAvailable(final WebContext context) {
        val webContext = (JEEContext) context;
        if (webContext != null) {
            val tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(webContext.getNativeRequest());

            if (StringUtils.isNotBlank(tgtId)) {
                val authentication = ticketRegistrySupport.getAuthenticationFrom(tgtId);
                if (authentication != null) {
                    return Optional.of(authentication);
                }
            }
        }
        return Optional.empty();
    }


    /**
     * Is valid issuer for endpoint.
     *
     * @param webContext the web context
     * @param endpoint   the endpoint
     * @return true /false
     */
    public boolean isValidIssuerForEndpoint(final JEEContext webContext, final String endpoint) {
        val requestUrl = webContext.getNativeRequest().getRequestURL().toString();
        val issuerFromRequestUrl = StringUtils.removeEnd(StringUtils.remove(requestUrl, '/' + endpoint), "/");
        val definedIssuer = oidcIssuerService.determineIssuer(Optional.empty());

        val definedIssuerWithSlash = StringUtils.appendIfMissing(definedIssuer, "/");
        val result = definedIssuer.equalsIgnoreCase(issuerFromRequestUrl) || issuerFromRequestUrl.startsWith(definedIssuerWithSlash);
        FunctionUtils.doIf(!result, o -> LOGGER.trace("Configured issuer [{}] defined does not match the request issuer [{}]",
            o, issuerFromRequestUrl)).accept(definedIssuer);
        return result;
    }
}
