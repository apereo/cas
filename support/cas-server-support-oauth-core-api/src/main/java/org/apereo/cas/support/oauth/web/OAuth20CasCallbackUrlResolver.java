package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.url.UrlResolver;

import java.util.ArrayList;
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

    @SneakyThrows
    private static Optional<NameValuePair> getQueryParameter(final WebContext context, final String name) {
        val value = OAuth20Utils.getRequestParameter(context, name)
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

    private static void addUrlParameter(final WebContext context, final URIBuilder builder, final String parameterName) {
        val parameter = getQueryParameter(context, parameterName);
        parameter.ifPresent(basicNameValuePair ->
            builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));
    }

    @Override
    @SneakyThrows
    public String compute(final String url, final WebContext context) {
        if (!url.startsWith(callbackUrl)) {
            return url;
        }

        val builder = new URIBuilder(url);

        addUrlParameter(context, builder, OAuth20Constants.CLIENT_ID);
        addUrlParameter(context, builder, OAuth20Constants.REDIRECT_URI);
        addUrlParameter(context, builder, OAuth20Constants.ACR_VALUES);
        addUrlParameter(context, builder, OAuth20Constants.RESPONSE_TYPE);
        addUrlParameter(context, builder, OAuth20Constants.GRANT_TYPE);
        addUrlParameter(context, builder, OAuth20Constants.RESPONSE_MODE);
        addUrlParameter(context, builder, OAuth20Constants.CLAIMS);
        addUrlParameter(context, builder, OAuth20Constants.REQUEST);
        addUrlParameter(context, builder, OAuth20Constants.STATE);
        addUrlParameter(context, builder, OAuth20Constants.NONCE);

        getIncludeParameterNames().forEach(param -> addUrlParameter(context, builder, param));

        val callbackResolved = builder.build().toString();

        LOGGER.debug("Final resolved callback URL is [{}]", callbackResolved);
        return callbackResolved;
    }

    /**
     * Gets include parameter names.
     *
     * @return the include parameter names
     */
    protected List<String> getIncludeParameterNames() {
        return new ArrayList<>(0);
    }
}
