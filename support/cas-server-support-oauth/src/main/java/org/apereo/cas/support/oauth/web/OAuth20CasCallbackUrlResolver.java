package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.UrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This is {@link OAuth20CasCallbackUrlResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OAuth20CasCallbackUrlResolver implements UrlResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20CasCallbackUrlResolver.class);

    private final String callbackUrl;

    public OAuth20CasCallbackUrlResolver(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    private static Optional<URIBuilder.BasicNameValuePair> getQueryParameter(final WebContext context, final String name) {
        final URIBuilder builderContext = new URIBuilder(context.getFullRequestURL());
        return builderContext.getQueryParams()
                .stream().filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public String compute(final String url, final WebContext context) {
        if (url.startsWith(callbackUrl)) {
            final URIBuilder builder = new URIBuilder(url, true);

            Optional<URIBuilder.BasicNameValuePair> parameter = getQueryParameter(context, OAuth20Constants.CLIENT_ID);
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            parameter = getQueryParameter(context, OAuth20Constants.REDIRECT_URI);
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            parameter = getQueryParameter(context, OAuth20Constants.ACR_VALUES);
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            parameter = getQueryParameter(context, OAuth20Constants.RESPONSE_TYPE);
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            parameter = getQueryParameter(context, OAuth20Constants.GRANT_TYPE);
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            final String callbackResolved = builder.build().toString();

            LOGGER.debug("Final resolved callback URL is [{}]", callbackResolved);
            return callbackResolved;
        }
        return url;
    }
}
