package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.YubiKeyMultifactorProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.http.HttpClient;

import com.yubico.client.v2.YubicoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class YubiKeyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    private final transient YubicoClient client;
    private final transient HttpClient httpClient;

    @Override
    protected boolean isAvailable() {
        try {
            val endpoints = client.getWsapiUrls();
            for (val endpoint : endpoints) {
                LOGGER.debug("Pinging YubiKey API endpoint at [{}]", endpoint);
                val msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                val message = msg != null ? msg.getMessage() : null;
                if (StringUtils.isNotBlank(message)) {
                    val response = EncodingUtils.urlDecode(message);
                    LOGGER.debug("Received YubiKey ping response [{}]", response);
                    return true;
                }
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "YubiKey";
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), YubiKeyMultifactorProperties.DEFAULT_IDENTIFIER);
    }
}
