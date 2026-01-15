package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

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
@SuperBuilder
public class SamlIdPMetadataDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = -705737727407407083L;
    /**
     * The Id.
     */
    @Id
    @Column(name = "id", columnDefinition = "BIGINT")
    @JsonProperty
    @Transient
    private long id;

    @Column(name = "appliesTo", unique = true, length = 512)
    @JsonProperty
    @Builder.Default
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
}
