package org.apereo.cas.support.saml.idp.metadata.jpa.oracle;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link OracleSamlIdPMetadataDocument}.
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
        column = @Column(columnDefinition = "clob")
    ),
    @AttributeOverride(
        name = "id",
        column = @Column(columnDefinition = "number")
    ),
    @AttributeOverride(
        name = "signingCertificate",
        column = @Column(columnDefinition = "varchar2(4000)")
    ),
    @AttributeOverride(
        name = "signingKey",
        column = @Column(columnDefinition = "varchar2(4000)")
    ),
    @AttributeOverride(
        name = "encryptionCertificate",
        column = @Column(columnDefinition = "varchar2(4000)")
    ),
    @AttributeOverride(
        name = "encryptionKey",
        column = @Column(columnDefinition = "varchar2(4000)")
    )
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class OracleSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
}

