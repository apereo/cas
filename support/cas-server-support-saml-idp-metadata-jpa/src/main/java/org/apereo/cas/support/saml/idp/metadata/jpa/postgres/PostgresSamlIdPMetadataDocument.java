package org.apereo.cas.support.saml.idp.metadata.jpa.postgres;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link PostgresSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "metadata", column = @Column(columnDefinition = "varchar")),
    @AttributeOverride(name = "signingCertificate", column = @Column(columnDefinition = "varchar")),
    @AttributeOverride(name = "signingKey", column = @Column(columnDefinition = "varchar")),
    @AttributeOverride(name = "encryptionCertificate", column = @Column(columnDefinition = "varchar")),
    @AttributeOverride(name = "encryptionKey", column = @Column(columnDefinition = "varchar"))
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
public class PostgresSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Serial
    private static final long serialVersionUID = 2576062504192441866L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SuppressWarnings("UnusedVariable")
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}

