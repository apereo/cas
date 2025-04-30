package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.duosecurity.Client;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * This is {@link DuoSecurityClientBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@UtilityClass
public class DuoSecurityClientBuilder {

    /**
     * Build client.
     *
     * @param casProperties the cas properties
     * @param properties    the properties
     * @return the client
     */
    public static Client build(final CasConfigurationProperties casProperties,
                               final DuoSecurityMultifactorAuthenticationProperties properties) {
        return FunctionUtils.doUnchecked(() -> {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            return new Client.Builder(
                resolver.resolve(properties.getDuoIntegrationKey()),
                resolver.resolve(properties.getDuoSecretKey()),
                resolver.resolve(properties.getDuoApiHost()),
                casProperties.getServer().getLoginUrl())
                .build();
        });
    }
}
