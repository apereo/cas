package org.apereo.cas.support.saml.idp.metadata.jpa.mysql;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

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
 * This is {@link MySQLSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "metadata", column = @Column(columnDefinition = "text")),
    @AttributeOverride(name = "signingCertificate", column = @Column(columnDefinition = "text")),
    @AttributeOverride(name = "signingKey", column = @Column(columnDefinition = "text")),
    @AttributeOverride(name = "encryptionCertificate", column = @Column(columnDefinition = "text")),
    @AttributeOverride(name = "encryptionKey", column = @Column(columnDefinition = "text"))
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
public class MySQLSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Serial
    private static final long serialVersionUID = 547459357921290008L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @SuppressWarnings("UnusedVariable")
    private long id = -1;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}

