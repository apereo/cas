package org.jasig.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.net.URLDecoder;

/**
 * The authentication provider for yubikey.
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("yubikeyAuthenticationProvider")
public class YubiKeyMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("yubiKeyAuthenticationHandler")
    private YubiKeyAuthenticationHandler yubiKeyAuthenticationHandler;

    /**
     * The Http client.
     */
    @NotNull
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Override
    public String buildIdentifier(final RegisteredService service) throws AuthenticationException {
        try {
            final String[] endpoints = yubiKeyAuthenticationHandler.getClient().getWsapiUrls();
            for (final String endpoint : endpoints) {
                logger.debug("Pinging YubiKey API endpoint at {}", endpoint);
                final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                if (msg != null && StringUtils.isNotBlank(msg.getMessage())) {
                    final String response = URLDecoder.decode(msg.getMessage(), "UTF-8");
                    logger.debug("Received YubiKey ping response {}", response);
                    return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
                }
            }
            throw new IllegalArgumentException("YubiKey WS API url cannot be reached");
        } catch (final Exception e) {
            if (service.getAuthenticationPolicy().isFailOpen()) {
                logger.warn("Duo could not be reached. Since the authentication provider is configured to fail-open, authentication will "
                        + "proceed without Duo for service {}. {}", service.getServiceId(), e);
                return null;
            }
        }
        throw new AuthenticationException();
    }
}
