package org.jasig.cas.adaptors.duo;

import com.duosecurity.duoweb.DuoWeb;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Component("duoAuthenticationService")
public final class DuoAuthenticationService {
    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String duoIntegrationKey;
    private final String duoSecretKey;
    private final String duoApplicationKey;
    private final String duoApiHost;

    /**
     * Creates the duo authentication service.
     * @param duoIntegrationKey duo integration key
     * @param duoSecretKey duo secret key
     * @param duoApplicationKey duo application key
     * @param duoApiHost duo API host url
     */
    @Autowired
    public DuoAuthenticationService(@NotNull @Value("${cas.duo.integration.key:}") final String duoIntegrationKey,
                                    @NotNull @Value("${cas.duo.secret.key:}") final String duoSecretKey,
                                    @NotNull @Value("${cas.duo.application.key:}") final String duoApplicationKey,
                                    @NotNull @Value("${cas.duo.api.host:}") final String duoApiHost) {

        if (StringUtils.isBlank(duoIntegrationKey)) {
            throw new IllegalArgumentException("Duo integration key cannot be blank");
        }
        if (StringUtils.isBlank(duoSecretKey)) {
            throw new IllegalArgumentException("Duo secret key cannot be blank");
        }
        if (StringUtils.isBlank(duoApplicationKey)) {
            throw new IllegalArgumentException("Duo application key cannot be blank");
        }
        if (StringUtils.isBlank(duoApiHost)) {
            throw new IllegalArgumentException("Duo api host cannot be blank");
        }

        this.duoIntegrationKey = duoIntegrationKey;
        this.duoSecretKey = duoSecretKey;
        this.duoApplicationKey = duoApplicationKey;
        this.duoApiHost = duoApiHost;
    }

    public String getDuoApiHost() {
        return this.duoApiHost;
    }

    public String getDuoIntegrationKey() {
        return duoIntegrationKey;
    }

    public String getDuoSecretKey() {
        return duoSecretKey;
    }

    public String getDuoApplicationKey() {
        return duoApplicationKey;
    }

    /**
     * Sign the authentication request.
     * @param username username requesting authentication
     * @return signed response
     */
    public String generateSignedRequestToken(final String username) {
        return DuoWeb.signRequest(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, username);
    }

    /**
     * Verify the authentication response from Duo.
     * @param signedRequestToken signed request token
     * @return authenticated user
     * @throws Exception if response verification fails
     */
    public String authenticate(final String signedRequestToken) throws Exception {
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }
        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, signedRequestToken);
    }
}
