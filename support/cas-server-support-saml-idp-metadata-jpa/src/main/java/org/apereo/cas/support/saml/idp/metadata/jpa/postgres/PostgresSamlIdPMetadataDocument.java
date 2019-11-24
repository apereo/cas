package org.apereo.cas.support.saml.idp.metadata.jpa.postgres;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link PostgresSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(
        name = "metadata",
        column = @Column(columnDefinition = "varchar")
    ),
    @AttributeOverride(
        name = "signingCertificate",
        column = @Column(columnDefinition = "varchar")
    ),
    @AttributeOverride(
        name = "signingKey",
        column = @Column(columnDefinition = "varchar")
    ),
    @AttributeOverride(
        name = "encryptionCertificate",
        column = @Column(columnDefinition = "varchar")
    ),
    @AttributeOverride(
        name = "encryptionKey",
        column = @Column(columnDefinition = "varchar")
    )
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class PostgresSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
}

