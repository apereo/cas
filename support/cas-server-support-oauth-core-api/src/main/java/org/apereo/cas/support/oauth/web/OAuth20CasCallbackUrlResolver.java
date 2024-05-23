package org.apereo.cas.support.oauth.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.url.UrlResolver;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OAuth20CasCallbackUrlResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20CasCallbackUrlResolver implements UrlResolver {
    private final String callbackUrl;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public String compute(final String url, final WebContext context) {
        if (!url.startsWith(callbackUrl)) {
            return url;
        }

        return FunctionUtils.doUnchecked(() -> {
            val builder = new URIBuilder(url);
            val includeParameterNames = getIncludeParameterNames();
            includeParameterNames.forEach(param -> addUrlParameter(context, builder, param));

            val existingParameters = new HashMap<>(context.getRequestParameters());
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

    private Optional<NameValuePair> getQueryParameter(final WebContext context, final String name) {
        val value = requestParameterResolver.resolveRequestParameter(context, name)
            .or(Unchecked.supplier(() -> {
                val builderContext = new URIBuilder(context.getFullRequestURL());
                return builderContext.getQueryParams()
                    .stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .map(NameValuePair::getValue)
                    .findFirst();
            }));
        return value.map(v -> new BasicNameValuePair(name, v));
    }

    private void addUrlParameter(final WebContext context, final URIBuilder builder,
                                 final String parameterName) {
        val parameter = getQueryParameter(context, parameterName);
        parameter.ifPresent(basicNameValuePair ->
            builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));
    }
}
