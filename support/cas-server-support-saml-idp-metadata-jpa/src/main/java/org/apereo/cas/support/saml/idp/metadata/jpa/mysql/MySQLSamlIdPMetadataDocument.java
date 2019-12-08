package org.apereo.cas.support.saml.idp.metadata.jpa.mysql;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link MySQLSamlIdPMetadataDocument}.
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
        column = @Column(columnDefinition = "text")
    ),
    @AttributeOverride(
        name = "signingCertificate",
        column = @Column(columnDefinition = "text")
    ),
    @AttributeOverride(
        name = "signingKey",
        column = @Column(columnDefinition = "text")
    ),
    @AttributeOverride(
        name = "encryptionCertificate",
        column = @Column(columnDefinition = "text")
    ),
    @AttributeOverride(
        name = "encryptionKey",
        column = @Column(columnDefinition = "text")
    )
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class MySQLSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
}

