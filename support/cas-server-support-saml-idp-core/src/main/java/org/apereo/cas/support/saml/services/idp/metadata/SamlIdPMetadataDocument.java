package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * This is {@link SamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Entity
@Table(name = "SamlIdPMetadataDocument")
@Getter
@Setter
@AllArgsConstructor
public class SamlIdPMetadataDocument {

    /**
     * The Id.
     */
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", columnDefinition = "BIGINT")
    @JsonProperty
    private long id = -1;

    @Column(name = "appliesTo", unique = true, length = 2_000)
    @JsonProperty
    private String appliesTo = "CAS";

    /**
     * The Metadata.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "metadata", length = 8_000)
    @JsonProperty
    private String metadata;

    /**
     * The Signing certificate.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "signingCertificate", length = 3_000)
    @JsonProperty
    private String signingCertificate;

    /**
     * The Signing key.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "signingKey", length = 3_000)
    @JsonProperty
    private String signingKey;

    /**
     * The Encryption certificate.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
    @Column(name = "encryptionCertificate", length = 3_000)
    @JsonProperty
    private String encryptionCertificate;

    /**
     * The Encryption key.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringNVarcharType")
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
