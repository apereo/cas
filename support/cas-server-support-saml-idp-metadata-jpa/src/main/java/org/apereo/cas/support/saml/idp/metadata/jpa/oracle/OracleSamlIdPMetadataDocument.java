package org.apereo.cas.support.saml.idp.metadata.jpa.oracle;

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
 * This is {@link OracleSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "metadata", column = @Column(columnDefinition = "clob")),
    @AttributeOverride(name = "id", column = @Column(columnDefinition = "number")),
    @AttributeOverride(name = "signingCertificate", column = @Column(columnDefinition = "varchar2(4000)")),
    @AttributeOverride(name = "signingKey", column = @Column(columnDefinition = "varchar2(4000)")),
    @AttributeOverride(name = "encryptionCertificate", column = @Column(columnDefinition = "varchar2(4000)")),
    @AttributeOverride(name = "encryptionKey", column = @Column(columnDefinition = "varchar2(4000)"))
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
public class OracleSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Serial
    private static final long serialVersionUID = 7087889980353544793L;

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

