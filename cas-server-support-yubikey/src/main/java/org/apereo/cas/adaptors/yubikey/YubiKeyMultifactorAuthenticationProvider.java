package org.apereo.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.util.http.HttpMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * The authentication provider for yubikey.
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("yubikeyAuthenticationProvider")
public class YubiKeyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Autowired
    @Qualifier("yubikeyAuthenticationHandler")
    private YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler;

    @Value("${cas.mfa.yubikey.rank:0}")
    private int rank;

    /**
     * The Http client.
     */
    
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;


    @Override
    public String getId() {
        return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }


    @Override
    protected boolean isAvailable() {
        try {
            final String[] endpoints = this.yubiKeyAuthenticationHandler.getClient().getWsapiUrls();
            for (final String endpoint : endpoints) {
                logger.debug("Pinging YubiKey API endpoint at {}", endpoint);
                final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                if (msg != null && StringUtils.isNotBlank(msg.getMessage())) {
                    final String response = EncodingUtils.urlDecode(msg.getMessage());
                    logger.debug("Received YubiKey ping response {}", response);
                    return true;
                }
            }
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }
}
