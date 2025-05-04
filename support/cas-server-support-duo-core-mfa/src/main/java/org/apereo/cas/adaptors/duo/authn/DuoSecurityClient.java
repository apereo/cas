package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.duosecurity.Client;
import com.duosecurity.client.Util;
import lombok.Getter;
import lombok.val;
import okhttp3.CertificatePinner;
import org.springframework.util.ReflectionUtils;
import java.util.Objects;

/**
 * This is {@link DuoSecurityClient}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class DuoSecurityClient {
    @Getter
    private final Client instance;
    private final DuoSecurityMultifactorAuthenticationProperties properties;

    public DuoSecurityClient(final String loginUrl, final DuoSecurityMultifactorAuthenticationProperties properties) {
        this.properties = properties;
        this.instance = buildClient(loginUrl);
    }

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
    private Client buildClient(final String loginUrl) {
        return FunctionUtils.doUnchecked(() -> {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val clientBuilder = new Client.Builder(
                resolver.resolve(getDuoIntegrationKey()),
                resolver.resolve(getDuoSecretKey()),
                resolver.resolve(getDuoApiHost()),
                loginUrl);
            return clientBuilder.build();
        });
    }

    /**
     * Create certificate pinner.
     *
     * @param host   the host
     * @param target the target
     * @return the certificate pinner
     */
    public CertificatePinner createCertificatePinner(final String host, final Object target) {
        val newCerts = fetchCaCerts(target);
        return Util.createPinner(host, newCerts);
    }

    private static String[] fetchCaCerts(final Object target) {
        val field = ReflectionUtils.findField(Client.Builder.class, "DEFAULT_CA_CERTS");
        Objects.requireNonNull(field, "Unable to extract default CA certs for Duo Security").trySetAccessible();
        return (String[]) ReflectionUtils.getField(field, target);
    }

}
