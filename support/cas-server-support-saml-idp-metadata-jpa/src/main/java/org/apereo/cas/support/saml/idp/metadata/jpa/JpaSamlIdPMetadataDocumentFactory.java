package org.apereo.cas.support.saml.idp.metadata.jpa;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.JpaSamlMetadataProperties;
import org.apereo.cas.support.saml.idp.metadata.jpa.generic.JpaSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.idp.metadata.jpa.oracle.OracleSamlIdPMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link JpaSamlIdPMetadataDocumentFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class JpaSamlIdPMetadataDocumentFactory {
    private final JpaSamlMetadataProperties properties;

    /**
     * New instance of saml metadata document.
     *
     * @return the saml metadata document
     */
    public SamlIdPMetadataDocument newInstance() {
        if (isOracle()) {
            return new OracleSamlIdPMetadataDocument();
        }
        return new JpaSamlIdPMetadataDocument();
    }

    private boolean isOracle() {
        return properties.getDialect().contains("Oracle");
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public Class getType() {
        if (isOracle()) {
            return OracleSamlIdPMetadataDocument.class;
        }
        return JpaSamlIdPMetadataDocument.class;
    }
}
