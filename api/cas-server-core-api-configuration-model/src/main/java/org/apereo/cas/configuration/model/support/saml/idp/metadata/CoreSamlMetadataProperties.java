package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CoreSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CoreSamlMetadataProperties")
public class CoreSamlMetadataProperties implements Serializable {

    private static final long serialVersionUID = -8116473583467202828L;

    /**
     * Whether invalid metadata should eagerly fail quickly on startup
     * once the resource is parsed.
     */
    private boolean failFast = true;

    /**
     * How long should metadata be cached.
     */
    @DurationCapable
    private String cacheExpiration = "PT24H";

    /**
     * Whether valid metadata is required.
     */
    private boolean requireValidMetadata = true;
}
