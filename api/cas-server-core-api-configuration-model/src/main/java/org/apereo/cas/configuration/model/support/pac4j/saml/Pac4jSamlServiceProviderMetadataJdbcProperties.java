package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link Pac4jSamlServiceProviderMetadataJdbcProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jSamlServiceProviderMetadataJdbcProperties")
public class Pac4jSamlServiceProviderMetadataJdbcProperties extends AbstractJpaProperties {
    @Serial
    private static final long serialVersionUID = -5114734720383722585L;

    /**
     * The table name in the database that holds the SAML2 service provider metadata.
     * The table structure and columns must be created and exist beforehand, and must
     * match the following SQL statements, with expected
     * adjustments depending on database type, driver and dialect:
     * <p>
     * {@code CREATE TABLE sp_metadata (entityId VARCHAR(512), metadata TEXT)}
     */
    @RequiredProperty
    private String tableName;
}
