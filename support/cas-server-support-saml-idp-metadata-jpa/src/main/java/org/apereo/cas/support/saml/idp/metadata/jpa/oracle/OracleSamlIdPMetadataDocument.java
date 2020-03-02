package org.apereo.cas.support.saml.idp.metadata.jpa.oracle;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is {@link OracleSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
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
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
public class OracleSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;
}

