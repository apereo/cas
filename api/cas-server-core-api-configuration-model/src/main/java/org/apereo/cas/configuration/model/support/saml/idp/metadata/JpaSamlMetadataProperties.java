package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for saml metadata based on JPA.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-jpa")
@Slf4j
@Getter
@Setter
public class JpaSamlMetadataProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 352435146313504995L;

    public JpaSamlMetadataProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-saml-metadata");
    }
}
