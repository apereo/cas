package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

/**
 * This is {@link RestSamlMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-rest")
@Slf4j
@Getter
@Setter
public class RestSamlMetadataProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -7734304585762871404L;
}
