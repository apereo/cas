package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link HttpSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("HttpSamlMetadataProperties")
public class HttpSamlMetadataProperties implements Serializable {

    private static final long serialVersionUID = -8226473583467202828L;

    /**
     * Forcefully download and fetch metadata files
     * form URL sources and disregard any cached copies
     * of the metadata.
     */
    private boolean forceMetadataRefresh = true;

    /**
     * Directory location where downloaded SAML metadata is cached
     * as backup files. If left undefined, the directory is calculated
     * off of the metadata location on disk when specified. The directory location
     * should also support and be resolvable via Spring expression language.
     */
    @ExpressionLanguageCapable
    private String metadataBackupLocation;
}
