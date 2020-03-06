package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * This is {@link SamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class SamlIdPMetadataDocument {

    /**
     * The Id.
     */
    @Id
    @Column(name = "id", columnDefinition = "BIGINT")
    @JsonProperty
    @Transient
    private long id = -1;

    @Column(name = "appliesTo", unique = true, length = 512)
    @JsonProperty
    private String appliesTo = "CAS";

    /**
     * The Metadata.
     */
    @Lob
    @Column(name = "metadata", length = 10_000)
    @JsonProperty
    private String metadata;

    /**
     * The Signing certificate.
     */
    @Lob
    @Column(name = "signingCertificate", length = 3_000)
    @JsonProperty
    private String signingCertificate;

    /**
     * The Signing key.
     */
    @Lob
    @Column(name = "signingKey", length = 3_000)
    @JsonProperty
    private String signingKey;

    /**
     * The Encryption certificate.
     */
    @Lob
    @Column(name = "encryptionCertificate", length = 3_000)
    @JsonProperty
    private String encryptionCertificate;

    /**
     * The Encryption key.
     */
    @Lob
    @Column(name = "encryptionKey", length = 3_000)
    @JsonProperty
    private String encryptionKey;

    /**
     * Instantiates a new Saml id p metadata document.
     */
    public SamlIdPMetadataDocument() {
        setId(System.nanoTime());
    }

    /**
     * Is this document valid and has any of the fields?
     *
     * @return true /false
     */
    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(getMetadata())
            && StringUtils.isNotBlank(getSigningCertificate())
            && StringUtils.isNotBlank(getSigningKey())
            && StringUtils.isNotBlank(getEncryptionCertificate())
            && StringUtils.isNotBlank(getEncryptionKey());
    }

    /**
     * Gets signing certificate decoded.
     *
     * @return the signing certificate decoded
     */
    @JsonIgnore
    public String getSigningCertificateDecoded() {
        if (EncodingUtils.isBase64(signingCertificate)) {
            val cert = SamlIdPMetadataGenerator.cleanCertificate(signingCertificate);
            return EncodingUtils.decodeBase64ToString(cert);
        }
        return signingCertificate;
    }

    /**
     * Gets encryption certificate decoded.
     *
     * @return the encryption certificate decoded
     */
    @JsonIgnore
    public String getEncryptionCertificateDecoded() {
        if (EncodingUtils.isBase64(encryptionCertificate)) {
            val cert = SamlIdPMetadataGenerator.cleanCertificate(encryptionCertificate);
            return EncodingUtils.decodeBase64ToString(cert);
        }
        return encryptionCertificate;
    }

    /**
     * Gets metadata decoded.
     *
     * @return the metadata decoded
     */
    @JsonIgnore
    public String getMetadataDecoded() {
        if (EncodingUtils.isBase64(metadata)) {
            return EncodingUtils.decodeBase64ToString(metadata);
        }
        return metadata;
    }
}
