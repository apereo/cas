package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.multihost.MultiHostUtils;

import lombok.RequiredArgsConstructor;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.http.url.UrlResolver;

/**
 * Specific {@link UrlResolver} handling multi-hosts configuration.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class MultiHostUrlResolver implements UrlResolver {

    private final CasConfigurationProperties casProperties;

    @Override
    public String compute(final String url, final WebContext context) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return MultiHostUtils.computeServerPrefix(casProperties) + url;
    }
}
