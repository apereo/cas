package org.apereo.cas.gauth;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.CoreGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import com.warrenstrange.googleauth.KeyRepresentation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultCasGoogleAuthenticator}.
 * A wrapper around the google authenticator instance {@link CasGoogleAuthenticator}
 * to allow for {@link org.springframework.cloud.context.config.annotation.RefreshScope}
 * on beans since original class is marked as final and cannot be refreshed
 * via proxies.
 * This uses the delegate pattern to route calls to the real instance.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class DefaultCasGoogleAuthenticator implements CasGoogleAuthenticator {
    private final CasConfigurationProperties casProperties;
    @Getter
    private final TenantExtractor tenantExtractor;

    @Getter
    @Setter
    private ICredentialRepository credentialRepository;

    @Override
    public GoogleAuthenticatorKey createCredentials() {
        return toAuthenticatorInstance().createCredentials();
    }

    @Override
    public GoogleAuthenticatorKey createCredentials(final String userName) {
        return toAuthenticatorInstance().createCredentials(userName);
    }

    @Override
    public boolean authorize(final String secret, final int verificationCode) {
        return toAuthenticatorInstance().authorize(secret, verificationCode);
    }

    @Override
    public int getTotpPassword(final String secret) {
        return toAuthenticatorInstance().getTotpPassword(secret);
    }

    protected IGoogleAuthenticator toAuthenticatorInstance() {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null && StringUtils.isNotBlank(clientInfo.getTenant())) {
            val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(clientInfo.getTenant()).orElseThrow();
            val bindingContext = tenantDefinition.bindProperties();
            if (bindingContext.isBound() && bindingContext.containsBindingFor(CoreGoogleAuthenticatorMultifactorProperties.class)) {
                val properties = bindingContext.value();
                val gauth = properties.getAuthn().getMfa().getGauth().getCore();
                return buildGoogleAuthenticatorFrom(gauth);
            }
        }
        
        val gauth = casProperties.getAuthn().getMfa().getGauth().getCore();
        return buildGoogleAuthenticatorFrom(gauth);
    }

    protected GoogleAuthenticator buildGoogleAuthenticatorFrom(
        final CoreGoogleAuthenticatorMultifactorProperties gauth) {
        val builder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        builder.setCodeDigits(gauth.getCodeDigits());
        builder.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(gauth.getTimeStepSize()));
        builder.setWindowSize(gauth.getWindowSize());
        builder.setKeyRepresentation(KeyRepresentation.BASE32);
        val authenticator = new GoogleAuthenticator(builder.build());
        authenticator.setCredentialRepository(credentialRepository);
        return authenticator;
    }
}
