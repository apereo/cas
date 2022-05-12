package com.yubico.webauthn.attestation;

/**
 * This is {@link Attestation}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Non-standardized representation of partly free-form information about an authenticator device.
 */
@Value
@Builder(toBuilder = true)
public class Attestation implements Serializable {

    private static final long serialVersionUID = -2324150228329213721L;

    /** A unique identifier for a particular version of the data source of the data in this object. */
    private final String metadataIdentifier;

    /** Free-form information about the authenticator vendor. */
    private final Map<String, String> vendorProperties;

    /** Free-form information about the authenticator model. */
    private final Map<String, String> deviceProperties;

    @JsonCreator
    private Attestation(
        @JsonProperty("metadataIdentifier") String metadataIdentifier,
        @JsonProperty("vendorProperties") Map<String, String> vendorProperties,
        @JsonProperty("deviceProperties") Map<String, String> deviceProperties) {
        this.metadataIdentifier = metadataIdentifier;
        this.vendorProperties = vendorProperties;
        this.deviceProperties = deviceProperties;
    }

    /** A unique identifier for a particular version of the data source of the data in this object. */
    public Optional<String> getMetadataIdentifier() {
        return Optional.ofNullable(metadataIdentifier);
    }

    /** Free-form information about the authenticator vendor. */
    public Optional<Map<String, String>> getVendorProperties() {
        return Optional.ofNullable(vendorProperties);
    }

    /** Free-form information about the authenticator model. */
    public Optional<Map<String, String>> getDeviceProperties() {
        return Optional.ofNullable(deviceProperties);
    }

    public static Attestation empty() {
        return builder().build();
    }

    public static class AttestationBuilder {
        private String metadataIdentifier;
        private Map<String, String> vendorProperties;
        private Map<String, String> deviceProperties;

        public AttestationBuilder metadataIdentifier(@NonNull Optional<String> metadataIdentifier) {
            return this.metadataIdentifier(metadataIdentifier.orElse(null));
        }

        public AttestationBuilder metadataIdentifier(String metadataIdentifier) {
            this.metadataIdentifier = metadataIdentifier;
            return this;
        }

        public AttestationBuilder vendorProperties(
            @NonNull Optional<Map<String, String>> vendorProperties) {
            return this.vendorProperties(vendorProperties.orElse(null));
        }

        public AttestationBuilder vendorProperties(Map<String, String> vendorProperties) {
            this.vendorProperties = vendorProperties;
            return this;
        }

        public AttestationBuilder deviceProperties(
            @NonNull Optional<Map<String, String>> deviceProperties) {
            return this.deviceProperties(deviceProperties.orElse(null));
        }

        public AttestationBuilder deviceProperties(Map<String, String> deviceProperties) {
            this.deviceProperties = deviceProperties;
            return this;
        }
    }
}

