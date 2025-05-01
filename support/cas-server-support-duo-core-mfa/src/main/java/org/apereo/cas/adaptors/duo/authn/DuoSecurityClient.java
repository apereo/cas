package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.duosecurity.Client;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link DuoSecurityClient}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DuoSecurityClient {
    private final String loginUrl;
    private final DuoSecurityMultifactorAuthenticationProperties properties;

    /**
     * Gets duo api host.
     *
     * @return the duo api host
     */
    public String getDuoApiHost() {
        return properties.getDuoApiHost();
    }

    /**
     * Gets duo integration key.
     *
     * @return the duo integration key
     */
    public String getDuoIntegrationKey() {
        return properties.getDuoIntegrationKey();
    }

    /**
     * Gets duo secret key.
     *
     * @return the duo secret key
     */
    public String getDuoSecretKey() {
        return properties.getDuoSecretKey();
    }

    /**
     * Build client.
     *
     * @return the client
     */
    public Client build() {
        return FunctionUtils.doUnchecked(() -> {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            return new Client.Builder(resolver.resolve(getDuoIntegrationKey()), resolver.resolve(getDuoSecretKey()),
                resolver.resolve(getDuoApiHost()), loginUrl).build();
        });
    }
}
