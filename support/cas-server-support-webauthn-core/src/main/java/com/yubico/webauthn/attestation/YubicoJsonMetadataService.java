package com.yubico.webauthn.attestation;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.CollectionUtil;
import com.yubico.webauthn.attestation.matcher.ExtensionMatcher;
import com.yubico.webauthn.attestation.matcher.FingerprintMatcher;
import com.yubico.webauthn.data.ByteArray;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link YubicoJsonMetadataService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class YubicoJsonMetadataService implements AttestationMetadataSource {

    private static final String SELECTORS = "selectors";

    private static final String SELECTOR_TYPE = "type";

    private static final String SELECTOR_PARAMETERS = "parameters";

    private static final Map<String, DeviceMatcher> DEFAULT_DEVICE_MATCHERS =
        Map.of(
            ExtensionMatcher.SELECTOR_TYPE, new ExtensionMatcher(),
            FingerprintMatcher.SELECTOR_TYPE, new FingerprintMatcher());

    private final Collection<MetadataObject> metadataObjects;

    private final Map<String, DeviceMatcher> matchers;

    private final Set<X509Certificate> trustRootCertificates;

    private YubicoJsonMetadataService(
        @NonNull
        final Collection<MetadataObject> metadataObjects,
        @NonNull
        final Map<String, DeviceMatcher> matchers) {
        this.trustRootCertificates =
            metadataObjects.stream()
                .flatMap(metadataObject -> metadataObject.getTrustedCertificates().stream())
                .map(
                    pemEncodedCert -> {
                        try {
                            return CertificateParser.parsePem(pemEncodedCert);
                        } catch (final CertificateException e) {
                            LOGGER.error("Failed to parse trusted certificate", e);
                            return null;
                        }
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        this.metadataObjects = metadataObjects;
        this.matchers = CollectionUtil.immutableMap(matchers);
    }

    public YubicoJsonMetadataService() {
        this(
            Stream.of(MetadataObject.readDefault(), MetadataObject.readPreview())
                .collect(Collectors.toList()),
            DEFAULT_DEVICE_MATCHERS);
    }

    public YubicoJsonMetadataService(
        @NonNull
        final Collection<MetadataObject> metadataObjects) {
        this(metadataObjects, DEFAULT_DEVICE_MATCHERS);
    }

    @Override
    public Optional<Attestation> findMetadata(final X509Certificate attestationCertificate) {
        return metadataObjects.stream()
            .map(metadata -> {
                Map<String, String> vendorProperties;
                Map<String, String> deviceProperties = null;
                String identifier;

                identifier = metadata.getIdentifier();
                vendorProperties = Maps.filterValues(metadata.getVendorInfo(), Objects::nonNull);
                for (val device : metadata.getDevices()) {
                    if (deviceMatches(device.get(SELECTORS), attestationCertificate)) {
                        ImmutableMap.Builder<String, String> devicePropertiesBuilder = ImmutableMap.builder();
                        for (final Map.Entry<String, JsonNode> deviceEntry : Lists.newArrayList(device.fields())) {
                            val value = deviceEntry.getValue();
                            if (value.isTextual()) {
                                devicePropertiesBuilder.put(deviceEntry.getKey(), value.asText());
                            }
                        }
                        deviceProperties = devicePropertiesBuilder.build();
                        break;
                    }
                }

                return Optional.ofNullable(deviceProperties)
                    .map(
                        deviceProps ->
                            Attestation.builder()
                                .metadataIdentifier(Optional.ofNullable(identifier))
                                .vendorProperties(Optional.of(vendorProperties))
                                .deviceProperties(deviceProps)
                                .build());
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findAny();
    }

    private boolean deviceMatches(
        final JsonNode selectors, @NonNull final X509Certificate attestationCertificate) {
        if (selectors == null || selectors.isNull()) {
            return true;
        } else {
            for (val selector : selectors) {
                val matcher = matchers.get(selector.get(SELECTOR_TYPE).asText());
                if (matcher != null
                    && matcher.matches(attestationCertificate, selector.get(SELECTOR_PARAMETERS))) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public TrustRootsResult findTrustRoots(
        final List<X509Certificate> attestationCertificateChain, final Optional<ByteArray> aaguid) {
        return TrustRootsResult.builder()
            .trustRoots(trustRootCertificates)
            .enableRevocationChecking(false)
            .build();
    }
}

