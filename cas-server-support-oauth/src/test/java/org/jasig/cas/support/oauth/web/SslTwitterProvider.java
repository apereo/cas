package org.jasig.cas.support.oauth.web;

import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthConfig;
import org.scribe.model.SignatureType;
import org.scribe.up.addon_to_scribe.ProxyOAuth10aServiceImpl;
import org.scribe.up.provider.impl.TwitterProvider;

/**
 * Twitter provider using SSL because Twitter now requires SSL.
 * Fixed in scribe-up 1.3.1.
 * 
 * @author Jerome Leleu
 * @since 3.5.3
 */
public class SslTwitterProvider extends TwitterProvider {

    @Override
    protected void internalInit() {
        this.service = new ProxyOAuth10aServiceImpl(new TwitterApi.Authenticate(),
                                                    new OAuthConfig(this.key, this.secret, this.callbackUrl,
                                                                    SignatureType.Header, null, null), this.proxyHost,
                                                    this.proxyPort);
    }
}
