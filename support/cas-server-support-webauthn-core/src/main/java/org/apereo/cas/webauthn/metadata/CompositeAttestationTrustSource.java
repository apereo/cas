package org.apereo.cas.webauthn.metadata;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.yubico.webauthn.attestation.AttestationTrustSource;
import com.yubico.webauthn.attestation.YubicoJsonMetadataService;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link CompositeAttestationTrustSource}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class CompositeAttestationTrustSource implements AttestationTrustSource {
    private final List<AttestationTrustSource> trustSources = new ArrayList<>();

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
    }


    @Override
    public TrustRootsResult findTrustRoots(final List<X509Certificate> list, final Optional<ByteArray> aaguid) {
        return trustSources
            .stream()
            .map(source -> source.findTrustRoots(list, aaguid))
            .filter(result -> result != null && !result.getTrustRoots().isEmpty())
            .findFirst()
            .orElseGet(() -> TrustRootsResult.builder().trustRoots(Set.of()).build());
    }

    @CanIgnoreReturnValue
    public CompositeAttestationTrustSource addAttestationTrustSource(final AttestationTrustSource source) {
        this.trustSources.add(source);
        return this;
    }
}
