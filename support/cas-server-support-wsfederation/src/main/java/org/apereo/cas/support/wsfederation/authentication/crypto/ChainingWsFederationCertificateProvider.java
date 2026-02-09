package org.apereo.cas.support.wsfederation.authentication.crypto;

import module java.base;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;

/**
 * This is {@link ChainingWsFederationCertificateProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public class ChainingWsFederationCertificateProvider implements WsFederationCertificateProvider {
    private final List<WsFederationCertificateProvider> providers = new ArrayList<>();

    /**
     * Add provider.
     *
     * @param provider the provider
     */
    public void addProvider(final WsFederationCertificateProvider provider) {
        this.providers.add(provider);
    }

    @Override
    public List<Credential> getSigningCredentials() {
        return providers.stream()
            .map(Unchecked.function(WsFederationCertificateProvider::getSigningCredentials))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
