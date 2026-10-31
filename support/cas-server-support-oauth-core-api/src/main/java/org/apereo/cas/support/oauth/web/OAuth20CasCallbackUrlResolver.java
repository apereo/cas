package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.jooq.lambda.Unchecked;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20CasCallbackUrlResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20CasCallbackUrlResolver {
    private static final String CLIENT_NAME_PARAMETER = "client_name";

    private final String callbackUrl;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    /**
     * The callback URL that the login handshake gives CAS as the service for the authorization request: the callback
     * endpoint with the parameters of the request, the ones that matter for the authorization first, and the
     * {@code client_name} that pac4j used to add, so the service of the tickets stays the same.
     *
     * @param request the authorization request
     * @return the callback URL
     */
    public String compute(final HttpServletRequest request) {
        val url = compute(callbackUrl, request);
        return Strings.CS.contains(url, CLIENT_NAME_PARAMETER + '=')
            ? url
            : url + (url.contains("?") ? '&' : '?') + CLIENT_NAME_PARAMETER + '=' + Authenticators.CAS_OAUTH_CLIENT;
    }

    /**
     * Add the parameters of the request to the URL, when the URL is the callback URL.
     *
     * @param url     the url
     * @param request the request
     * @return the url with the parameters of the request
     */
    public String compute(final String url, final HttpServletRequest request) {
        if (!url.startsWith(callbackUrl)) {
            return url;
        }
        return FunctionUtils.doUnchecked(() -> {
            val builder = new URIBuilder(url);
            val includeParameterNames = getIncludeParameterNames();
            includeParameterNames.forEach(param -> addUrlParameter(request, builder, param));

            val existingParameters = new HashMap<>(request.getParameterMap());
            includeParameterNames.forEach(existingParameters.keySet()::remove);
            existingParameters.keySet().removeAll(getExcludedParameterNames());
            existingParameters
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().length > 0)
                .forEach(entry -> builder.addParameter(entry.getKey(), entry.getValue()[0]));

            val callbackResolved = builder.build().toString();
            LOGGER.debug("Final resolved callback URL is [{}]", callbackResolved);
            return callbackResolved;
        });
    }

    /**
     * The service that the ticket at the callback was issued for: the callback URL with the query of the request, less the
     * {@code ticket} parameter that CAS appended, in the order CAS received it.
     *
     * @param request the callback request
     * @return the service of the ticket
     */
    public String computeServiceForTicket(final HttpServletRequest request) {
        val query = Stream.ofNullable(request.getQueryString())
            .flatMap(value -> Arrays.stream(value.split("&")))
            .filter(parameter -> !parameter.startsWith(CasProtocolConstants.PARAMETER_TICKET + '='))
            .collect(Collectors.joining("&"));
        return query.isEmpty() ? callbackUrl : callbackUrl + '?' + query;
    }

    private static Collection<String> getExcludedParameterNames() {
        return List.of(
            CasProtocolConstants.PARAMETER_SERVICE,
            CasProtocolConstants.PARAMETER_TARGET_SERVICE,
            CasProtocolConstants.PARAMETER_TICKET
        );
    }

    protected List<String> getIncludeParameterNames() {
        return CollectionUtils.wrapList(
            OAuth20Constants.CLIENT_ID,
            OAuth20Constants.SCOPE,
            OAuth20Constants.REDIRECT_URI,
            OAuth20Constants.ACR_VALUES,
            OAuth20Constants.RESPONSE_TYPE,
            OAuth20Constants.GRANT_TYPE,
            OAuth20Constants.RESPONSE_MODE,
            OAuth20Constants.CLAIMS,
            OAuth20Constants.REQUEST,
            OAuth20Constants.STATE,
            OAuth20Constants.NONCE,
            TokenConstants.PARAMETER_NAME_TOKEN
        );
    }

    private Optional<NameValuePair> getQueryParameter(final HttpServletRequest request, final String name) {
        val value = requestParameterResolver.resolveRequestParameter(request, name)
            .or(Unchecked.supplier(() -> {
                val builderContext = new URIBuilder(HttpRequestUtils.getFullRequestUrl(request));
                return builderContext.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .map(NameValuePair::getValue)
                    .findFirst();
            }));
        return value.map(v -> new BasicNameValuePair(name, v));
    }

    private void addUrlParameter(final HttpServletRequest request, final URIBuilder builder,
                                 final String parameterName) {
        val parameter = getQueryParameter(request, parameterName);
        parameter.ifPresent(basicNameValuePair ->
            builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));
    }
}
