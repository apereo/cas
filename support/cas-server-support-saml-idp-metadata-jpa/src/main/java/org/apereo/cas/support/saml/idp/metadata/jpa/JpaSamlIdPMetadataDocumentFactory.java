package org.apereo.cas.support.saml.idp.metadata.jpa;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.JpaSamlMetadataProperties;
import org.apereo.cas.support.saml.idp.metadata.jpa.generic.JpaSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.mysql.MySQLSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.oracle.OracleSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.postgres.PostgresSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This is {@link JpaSamlIdPMetadataDocumentFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Getter
public class JpaSamlIdPMetadataDocumentFactory {
    private final JpaSamlMetadataProperties properties;

    /**
     * New instance of saml metadata document.
     *
     * @return the saml metadata document
     */
    @SneakyThrows
    public SamlIdPMetadataDocument newInstance() {
        return getType().getDeclaredConstructor().newInstance();
    }

    private boolean isOracle() {
        return properties.getDialect().contains("Oracle");
    }

    private boolean isMySql() {
        return properties.getDialect().contains("MySQL");
    }

    private boolean isPostgres() {
        return properties.getUrl().contains("postgresql");
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Class<? extends SamlIdPMetadataDocument> getType() {
        if (isOracle()) {
            return OracleSamlIdPMetadataDocument.class;
        }
        if (isMySql()) {
            return MySQLSamlIdPMetadataDocument.class;
        }
        if (isPostgres()) {
            return PostgresSamlIdPMetadataDocument.class;
        }
        return JpaSamlIdPMetadataDocument.class;
    }
}
