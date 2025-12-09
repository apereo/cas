package com.yubico.webauthn.attestation;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link Attestation}.
 * Non-standardized representation of partly free-form information about an authenticator device.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Value
@Builder(toBuilder = true)
public class Attestation implements Serializable {

    @Serial
    private static final long serialVersionUID = -2324150228329213721L;

    /**
     * A unique identifier for a particular version of the data source of the data in this object.
     */
    String metadataIdentifier;

    /**
     * Free-form information about the authenticator vendor.
     */
    Map<String, String> vendorProperties;

    /**
     * Free-form information about the authenticator model.
     */
    Map<String, String> deviceProperties;

    @JsonCreator
    private Attestation(
        @JsonProperty("metadataIdentifier") final
        String metadataIdentifier,
        @JsonProperty("vendorProperties") final
        Map<String, String> vendorProperties,
        @JsonProperty("deviceProperties") final
        Map<String, String> deviceProperties) {
        this.metadataIdentifier = metadataIdentifier;
        this.vendorProperties = vendorProperties;
        this.deviceProperties = deviceProperties;
    }

    /**
     * A unique identifier for a particular version of the data source of the data in this object.
     */
    public Optional<String> getMetadataIdentifier() {
        return Optional.ofNullable(metadataIdentifier);
    }

    /**
     * Free-form information about the authenticator vendor.
     */
    public Optional<Map<String, String>> getVendorProperties() {
        return Optional.ofNullable(vendorProperties);
    }

    /**
     * Free-form information about the authenticator model.
     */
    public Optional<Map<String, String>> getDeviceProperties() {
        return Optional.ofNullable(deviceProperties);
    }

    public static Attestation empty() {
        return builder().build();
    }

    public static class AttestationBuilder {
        private String metadataIdentifier;

        private Map<String, String> vendorProperties = new HashMap<>();

        private Map<String, String> deviceProperties = new HashMap<>();

        @CanIgnoreReturnValue
        public AttestationBuilder metadataIdentifier(
            @NonNull
            final Optional<String> metadataIdentifier) {
            return this.metadataIdentifier(metadataIdentifier.orElse(null));
        }

        @CanIgnoreReturnValue
        public AttestationBuilder metadataIdentifier(final String metadataIdentifier) {
            this.metadataIdentifier = metadataIdentifier;
            return this;
        }

        @CanIgnoreReturnValue
        public AttestationBuilder vendorProperties(
            @NonNull final Optional<Map<String, String>> vendorProperties) {
            return this.vendorProperties(vendorProperties.orElse(null));
        }

        @CanIgnoreReturnValue
        public AttestationBuilder vendorProperties(final Map<String, String> vendorProperties) {
            this.vendorProperties = vendorProperties;
            return this;
        }

        @CanIgnoreReturnValue
        public AttestationBuilder deviceProperties(
            @NonNull
            final Optional<Map<String, String>> deviceProperties) {
            return this.deviceProperties(deviceProperties.orElse(null));
        }

        @CanIgnoreReturnValue
        public AttestationBuilder deviceProperties(final Map<String, String> deviceProperties) {
            this.deviceProperties = deviceProperties;
            return this;
        }
    }
}

