package org.apereo.cas.oidc.util;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /**
     * Gets oidc max age from authorization request.
     *
     * @param request the request
     * @return the oidc max age from authorization request
     */
    public static Optional<Long> getOidcMaxAgeFromAuthorizationRequest(final HttpServletRequest request) {
        return FunctionUtils.doUnchecked(() -> {
            val builderContext = new URIBuilder(HttpRequestUtils.getFullRequestUrl(request)).setPlusAsBlank(true);
            return builderContext.getQueryParams()
                .stream()
                .filter(p -> OidcConstants.MAX_AGE.equals(p.getName()))
                .map(p -> Optional.of(p.getValue()))
                .findFirst()
                .orElseGet(() -> Optional.ofNullable(request.getParameter(OidcConstants.MAX_AGE)))
                .map(param -> {
                    val maxAge = NumberUtils.toLong(param, -1);
                    return Optional.of(maxAge);
                })
                .orElseGet(Optional::empty);
        });
    }

    /**
     * The {@code max_age} of the authorization request, from its URL.
     *
     * @param url the url of the authorization request
     * @return the max age, or {@code -1} when it is not a number
     */
    public static Optional<Long> getOidcMaxAgeFromAuthorizationRequest(final String url) {
        return FunctionUtils.doAndHandle(() -> new URIBuilder(url).setPlusAsBlank(true).getQueryParams()
            .stream()
            .filter(param -> OidcConstants.MAX_AGE.equals(param.getName()))
            .findFirst()
            .map(param -> NumberUtils.toLong(param.getValue(), -1)), _ -> Optional.<Long>empty()).get();
    }

    /**
     * Is authentication profile available?.
     *
     * @param request      the request
     * @param response     the response
     * @param sessionStore the session store
     * @return the optional user profile
     */
    public static Optional<OAuth20UserProfile> isAuthenticationProfileAvailable(final HttpServletRequest request,
                                                                                final HttpServletResponse response,
                                                                                final CasSessionStore sessionStore) {
        return OAuth20Utils.findAuthenticatedUserProfile(request, response, sessionStore);
    }

    /**
     * Remove a value from the {@code prompt} of the authorization request, and the parameter when no value is left. The
     * query is read as a browser form-encodes it, with {@code +} as a space, so the other parameters keep their values
     * (a scope of {@code openid+profile} stays two scopes, and the state is unchanged).
     *
     * @param url    the url
     * @param prompt the prompt
     * @return the string
     */
    public static String removeOidcPromptFromAuthorizationRequest(final String url, final String prompt) {
        return FunctionUtils.doUnchecked(() -> {
            val uriBuilder = new URIBuilder(url).setPlusAsBlank(true);
            val newParams = uriBuilder.getQueryParams()
                .stream()
                .map(param -> {
                    if (!OAuth20Constants.PROMPT.equals(param.getName())) {
                        return param;
                    }
                    val remaining = Arrays.stream(StringUtils.split(Objects.toString(param.getValue(), StringUtils.EMPTY), ' '))
                        .filter(value -> !value.equalsIgnoreCase(prompt))
                        .collect(Collectors.joining(" "));
                    return remaining.isEmpty() ? null : new BasicNameValuePair(param.getName(), remaining);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            return uriBuilder
                .removeQuery()
                .addParameters(newParams)
                .build()
                .toASCIIString();
        });
    }

    /**
     * Whether the authorization request asks for a fresh login with {@code max_age=0}, which OpenID Connect Core, section
     * 3.1.2.1, makes equivalent to {@code prompt=login}.
     *
     * @param url the url of the authorization request
     * @return true or false
     */
    public static boolean isZeroMaxAgeAuthorizationRequest(final String url) {
        return FunctionUtils.doAndHandle(() -> new URIBuilder(url).setPlusAsBlank(true).getQueryParams()
            .stream()
            .anyMatch(param -> OidcConstants.MAX_AGE.equals(param.getName()) && "0".equals(StringUtils.trim(param.getValue()))), _ -> false).get();
    }

    /**
     * Whether the authorization request asks for a fresh login with {@code max_age=0}.
     *
     * @param request the request
     * @return true or false
     */
    public static boolean isZeroMaxAgeAuthorizationRequest(final HttpServletRequest request) {
        return getOidcMaxAgeFromAuthorizationRequest(request).filter(maxAge -> maxAge == 0).isPresent();
    }

    /**
     * Remove {@code max_age=0} from the authorization request, reading the query as a browser form-encodes it.
     *
     * @param url the url of the authorization request
     * @return the url
     */
    public static String removeZeroMaxAgeFromAuthorizationRequest(final String url) {
        return FunctionUtils.doUnchecked(() -> {
            val uriBuilder = new URIBuilder(url).setPlusAsBlank(true);
            val newParams = uriBuilder.getQueryParams()
                .stream()
                .filter(param -> !OidcConstants.MAX_AGE.equals(param.getName()) || !"0".equals(StringUtils.trim(param.getValue())))
                .collect(Collectors.toList());
            return uriBuilder
                .removeQuery()
                .addParameters(newParams)
                .build()
                .toASCIIString();
        });
    }

    /**
     * Is cas authentication old for max age authorization request boolean.
     *
     * @param request            the request
     * @param authenticationDate the authentication date
     * @return true/false
     */
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final HttpServletRequest request,
                                                                              final ZonedDateTime authenticationDate) {
        return isOlderThanMaxAge(authenticationDate, getOidcMaxAgeFromAuthorizationRequest(request));
    }

    /**
     * Determines whether CAS authentication exceeds the authorization request's maximum age.
     *
     * @param request        the request
     * @param authentication the authentication
     * @return true/false
     */
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final HttpServletRequest request,
                                                                              final Authentication authentication) {
        return isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, authentication.getAuthenticationDate());
    }

    /**
     * Determines whether CAS authentication exceeds the authorization request's maximum age.
     *
     * @param request the request
     * @param profile the profile
     * @return true/false
     */
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final HttpServletRequest request,
                                                                              final OAuth20UserProfile profile) {
        return getAuthenticationDate(profile)
            .filter(authenticationDate -> isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, authenticationDate))
            .isPresent();
    }

    /**
     * Determines whether the authentication behind the profile exceeds the {@code max_age} of the authorization request
     * with the given URL, such as the request that the login handshake resumes.
     *
     * @param url     the url of the authorization request
     * @param profile the profile
     * @return true/false
     */
    public static boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final String url, final OAuth20UserProfile profile) {
        return getAuthenticationDate(profile)
            .filter(authenticationDate -> isOlderThanMaxAge(authenticationDate, getOidcMaxAgeFromAuthorizationRequest(url)))
            .isPresent();
    }

    /**
     * Determines whether CAS authentication is available and exceeds the authorization request's maximum age.
     *
     * @param request the request
     * @return true/false
     */
    public boolean isCasAuthenticationOldForMaxAgeAuthorizationRequest(final HttpServletRequest request) {
        return isCasAuthenticationAvailable(request)
            .filter(a -> isCasAuthenticationOldForMaxAgeAuthorizationRequest(request, a))
            .isPresent();
    }

    private static Optional<ZonedDateTime> getAuthenticationDate(final OAuth20UserProfile profile) {
        var authTime = profile.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        if (authTime == null) {
            authTime = profile.getAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        }
        return Optional.ofNullable(authTime)
            .map(value -> ZonedDateTime.parse(CollectionUtils.toCollection(value).iterator().next().toString()));
    }

    private static boolean isOlderThanMaxAge(final ZonedDateTime authenticationDate, final Optional<Long> maxAge) {
        if (maxAge.isPresent() && maxAge.get() > 0) {
            val now = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
            val authTime = authenticationDate.toEpochSecond();
            val diffInSeconds = now - authTime;
            if (diffInSeconds > maxAge.get()) {
                LOGGER.info("Authentication is too old: [{}] and was created [{}] seconds ago.", authTime, diffInSeconds);
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether CAS authentication is available.
     *
     * @param request the request
     * @return the optional authn
     */
    public Optional<Authentication> isCasAuthenticationAvailable(final HttpServletRequest request) {
        if (request != null) {
            val tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);

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
