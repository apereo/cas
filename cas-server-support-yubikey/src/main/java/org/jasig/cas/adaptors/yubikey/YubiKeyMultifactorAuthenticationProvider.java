package org.jasig.cas.adaptors.yubikey;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceMultifactorPolicy;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${cas.mfa.yubikey.rank:0}")
    private int rank;

    /**
     * The Http client.
     */
    @NotNull
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Override
    public boolean verify(final RegisteredService service) throws AuthenticationException {
        try {
            final String[] endpoints = yubiKeyAuthenticationHandler.getClient().getWsapiUrls();
            for (final String endpoint : endpoints) {
                logger.debug("Pinging YubiKey API endpoint at {}", endpoint);
                final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(endpoint));
                if (msg != null && StringUtils.isNotBlank(msg.getMessage())) {
                    final String response = URLDecoder.decode(msg.getMessage(), "UTF-8");
                    logger.debug("Received YubiKey ping response {}", response);
                    return true;
                }
            }
            throw new IllegalArgumentException("YubiKey WS API url cannot be reached");
        } catch (final Exception e) {
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() == RegisteredServiceMultifactorPolicy.FailureModes.OPEN) {
                logger.warn("Yubico could not be reached. Since the authentication provider is configured to fail-open, authentication "
                       +  "will proceed without Duo for service {}. {}", service.getServiceId(), e);
                return false;
            }
        }
        throw new AuthenticationException();
    }

    @Override
    public String getId() {
        return YubiKeyMultifactorWebflowConfigurer.MFA_YUBIKEY_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final YubiKeyMultifactorAuthenticationProvider rhs = (YubiKeyMultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .append(this.rank, rhs.rank)
                .append(this.getId(), rhs.getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rank)
                .append(getId())
                .toHashCode();
    }
}
