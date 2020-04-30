package org.apereo.cas.oidc.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
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
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcAuthorizationRequestSupport}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcAuthorizationRequestSupport {
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final TicketRegistrySupport ticketRegistrySupport;

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
        val parameter = builderContext.getQueryParams()
            .stream().filter(p -> OidcConstants.MAX_AGE.equals(p.getName()))
            .findFirst();

        if (parameter.isPresent()) {
            val maxAge = NumberUtils.toLong(parameter.get().getValue(), -1);
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
    public static Optional<CommonProfile> isAuthenticationProfileAvailable(final JEEContext context) {
        val manager = new ProfileManager<CommonProfile>(context, context.getSessionStore());
        return manager.get(true);
    }

    @SneakyThrows
    public static String getRedirectUrlWithError(final String originalRedirectUrl, final String errorCode) {
        val uriBuilder = new URIBuilder(originalRedirectUrl)
            .addParameter(OAuth20Constants.ERROR, errorCode);
        return uriBuilder.build().toASCIIString();
    }

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
                                                                              final CommonProfile profile) {
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


}
