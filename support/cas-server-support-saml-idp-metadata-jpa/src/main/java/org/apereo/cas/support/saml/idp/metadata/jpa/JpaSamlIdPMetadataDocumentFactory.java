package org.apereo.cas.support.saml.idp.metadata.jpa;

import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.support.saml.idp.metadata.jpa.generic.JpaSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.mysql.MySQLSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.oracle.OracleSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.postgres.PostgresSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

/**
 * This is {@link JpaSamlIdPMetadataDocumentFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class JpaSamlIdPMetadataDocumentFactory extends AbstractJpaEntityFactory<SamlIdPMetadataDocument> {
    public JpaSamlIdPMetadataDocumentFactory(final String dialect) {
        super(dialect);
    }

    @Override
    public Class<SamlIdPMetadataDocument> getType() {
        return (Class<SamlIdPMetadataDocument>) getEntityClass();
    }

    private Class<? extends SamlIdPMetadataDocument> getEntityClass() {
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
