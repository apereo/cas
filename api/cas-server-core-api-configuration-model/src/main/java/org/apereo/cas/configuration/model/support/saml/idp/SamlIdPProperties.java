package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.SamlIdPMetadataProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link SamlIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SamlIdPProperties")
public class SamlIdPProperties implements Serializable {

    private static final long serialVersionUID = -5848075783676789852L;

    /**
     * Core SAML2 settings that control key
     * aspects of the saml2 authentication scenario.
     */
    @NestedConfigurationProperty
    private SamlIdPCoreProperties core = new SamlIdPCoreProperties();
    
    /**
     * Settings related to SAML2 responses.
     */
    @NestedConfigurationProperty
    private SamlIdPResponseProperties response = new SamlIdPResponseProperties();

    /**
     * SAML2 metadata related settings.
     */
    @NestedConfigurationProperty
    private SamlIdPMetadataProperties metadata = new SamlIdPMetadataProperties();

    /**
     * SAML2 logout related settings.
     */
    @NestedConfigurationProperty
    private SamlIdPLogoutProperties logout = new SamlIdPLogoutProperties();

    /**
     * Settings related to algorithms used for signing, etc.
     */
    @NestedConfigurationProperty
    private SamlIdPAlgorithmsProperties algs = new SamlIdPAlgorithmsProperties();

    /**
     * Settings related to naming saml cache storage.
     */
    @NestedConfigurationProperty
    private SamlIdPTicketProperties ticket = new SamlIdPTicketProperties();

    /**
     * Settings related to handling saml2 profiles.
     */
    @NestedConfigurationProperty
    private SamlIdPProfileProperties profile = new SamlIdPProfileProperties();
}
