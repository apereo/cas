package org.apereo.cas.support.openid.web.support;


/**
 * Extracts a local Id from an openid.identity. The default provider can extract
 * the following uris: http://openid.myprovider.com/scottb provides a local id
 * of scottb.
 *
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@Deprecated(since = "6.2.0")
public class DefaultOpenIdUserNameExtractor implements OpenIdUserNameExtractor {

    @Override
    public String extractLocalUsernameFromUri(final String uri) {
        if (uri == null) {
            return null;
        }

        if (!uri.contains("/")) {
            return null;
        }

        return uri.substring(uri.lastIndexOf('/') + 1);
    }

}
