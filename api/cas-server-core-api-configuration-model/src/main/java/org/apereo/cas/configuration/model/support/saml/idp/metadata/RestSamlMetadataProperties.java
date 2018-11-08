package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link RestSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-rest")
@Getter
@Setter
public class RestSamlMetadataProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -7734304585762871404L;
}
