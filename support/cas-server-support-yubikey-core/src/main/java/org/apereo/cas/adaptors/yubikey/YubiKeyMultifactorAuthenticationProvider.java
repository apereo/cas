package org.apereo.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(YubiKeyMultifactorAuthenticationProvider.class);
    
    private static final long serialVersionUID = 4789727148634156909L;

    private final YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler;

    private final HttpClient httpClient;

    public YubiKeyMultifactorAuthenticationProvider(final YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler,
                                                    final HttpClient httpClient) {
        this.yubiKeyAuthenticationHandler = yubiKeyAuthenticationHandler;
        this.httpClient = httpClient;
    }

    @Override
    protected boolean isAvailable() {
        try {
            final String[] endpoints = this.yubiKeyAuthenticationHandler.getClient().getWsapiUrls();
            for (final String endpoint : endpoints) {
                LOGGER.debug("Pinging YubiKey API endpoint at [{}]", endpoint);
                final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                final String message = msg != null ? msg.getMessage() : null;
                if (StringUtils.isNotBlank(message)) {
                    final String response = EncodingUtils.urlDecode(message);
                    LOGGER.debug("Received YubiKey ping response [{}]", response);
                    return true;
                }
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }
}
