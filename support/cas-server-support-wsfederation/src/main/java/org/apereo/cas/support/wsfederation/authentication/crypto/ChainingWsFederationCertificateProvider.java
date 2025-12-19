package org.apereo.cas.support.wsfederation.authentication.crypto;

import module java.base;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;

/**
 * This is {@link ChainingWsFederationCertificateProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
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
