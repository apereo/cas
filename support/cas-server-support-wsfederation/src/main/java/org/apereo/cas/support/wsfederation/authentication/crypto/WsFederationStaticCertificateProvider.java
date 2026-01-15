package org.apereo.cas.support.wsfederation.authentication.crypto;

import module java.base;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;
import org.springframework.core.io.Resource;

/**
 * This is {@link WsFederationStaticCertificateProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class WsFederationStaticCertificateProvider implements WsFederationCertificateProvider {
    private final Resource signingCertificate;

    @Override
    public List<Credential> getSigningCredentials() {
        return Stream.of(signingCertificate)
            .map(Unchecked.function(c -> WsFederationCertificateProvider.readCredential(c.getInputStream())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
