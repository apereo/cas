package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuthConstants;
import org.jasig.cas.client.util.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.CallbackUrlResolver;

import java.util.Optional;

/**
 * This is {@link OAuth20CasCallbackUrlResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OAuth20CasCallbackUrlResolver implements CallbackUrlResolver {
    private String callbackUrl;

    public OAuth20CasCallbackUrlResolver(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String compute(final String url, final WebContext context) {
        if (url.startsWith(callbackUrl)) {
            final URIBuilder builder = new URIBuilder(url);
            final URIBuilder builderContext = new URIBuilder(context.getFullRequestURL());
            Optional<URIBuilder.BasicNameValuePair> parameter = builderContext.getQueryParams()
                    .stream().filter(p -> p.getName().equals(OAuthConstants.CLIENT_ID))
                    .findFirst();

            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));
            parameter = builderContext.getQueryParams()
                    .stream().filter(p -> p.getName().equals(OAuthConstants.REDIRECT_URI))
                    .findFirst();
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));

            parameter = builderContext.getQueryParams()
                    .stream().filter(p -> p.getName().equals(OAuthConstants.ACR_VALUES))
                    .findFirst();
            parameter.ifPresent(basicNameValuePair -> builder.addParameter(basicNameValuePair.getName(), basicNameValuePair.getValue()));
            return builder.build().toString();
        }
        return url;
    }
}
