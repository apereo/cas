package org.apereo.cas.support.saml.idp.metadata.jpa.generic;

import module java.base;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * This is {@link JpaSamlIdPMetadataDocument}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity(name = "SamlIdPMetadataDocument")
@Table(name = "SamlIdPMetadataDocument")
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
public class JpaSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Serial
    private static final long serialVersionUID = -7865710977205378149L;

    @Id
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}

