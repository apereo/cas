package org.apereo.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;

import java.net.URL;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private final YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler;

    private final HttpClient httpClient;

    public YubiKeyMultifactorAuthenticationProvider(final YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler,
                                                    final HttpClient httpClient) {
        this.yubiKeyAuthenticationHandler = yubiKeyAuthenticationHandler;
        this.httpClient = httpClient;
    }

    @Override
    public String getId() {
        return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return casProperties.getAuthn().getMfa().getYubikey().getRank();
    }


    @Override
    protected boolean isAvailable() {
        try {
            final String[] endpoints = this.yubiKeyAuthenticationHandler.getClient().getWsapiUrls();
            for (final String endpoint : endpoints) {
                logger.debug("Pinging YubiKey API endpoint at {}", endpoint);
                final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                final String message = msg.getMessage();
                if (msg != null && StringUtils.isNotBlank(message)) {
                    final String response = EncodingUtils.urlDecode(message);
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
