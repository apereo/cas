package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
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
    private static final String[] CA_CERTS_V12 = {
        "sha256/I/Lt/z7ekCWanjD0Cvj5EqXls2lOaThEA0H2Bg4BT/o=",
        "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=",
        "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=",
        "sha256/dykHF2FLJfEpZOvbOLX4PKrcD2w2sHd/iA/G3uHTOcw=",
        "sha256/JZaQTcTWma4gws703OR/KFk313RkrDcHRvUt6na6DCg=",
        "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=",
        "sha256/f0KW/FtqTjs108NpYj42SrGvOB2PpxIVM8nWxjPqJGE=",
        "sha256/NqvDJlas/GRcYbcWE8S/IceH9cq77kg0jVhZeAPXq8k=",
        "sha256/9+ze1cZgR9KO1kZrVDxA4HQ6voHRCSVNz4RdTCx4U8U=",
        "sha256/j9ESw8g3DxR9XM06fYZeuN1UB4O6xp/GAIjjdD/zM3g="
    };

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
            val newCerts = fetchCaCerts(clientBuilder);
            clientBuilder.setCACerts(newCerts);

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
        Objects.requireNonNull(field).trySetAccessible();
        val currentCaCerts = (String[]) ReflectionUtils.getField(field, target);
        return CollectionUtils.combineArrays(currentCaCerts, CA_CERTS_V12);
    }

}
