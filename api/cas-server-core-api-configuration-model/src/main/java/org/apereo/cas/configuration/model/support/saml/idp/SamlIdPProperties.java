package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.SamlIdPMetadataProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class SamlIdPProperties implements Serializable {

    private static final long serialVersionUID = -5848075783676789852L;

    /**
     * Indicates whether attribute query profile is enabled.
     * Enabling this setting would allow CAS to record SAML
     * responses and have them be made available later for attribute lookups.
     */
    private boolean attributeQueryProfileEnabled;

    /**
     * The SAML entity id for the deployment.
     */
    @RequiredProperty
    private String entityId = "https://cas.example.org/idp";

    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are references and mapped one ones that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     */
    private List<String> authenticationContextClassMappings = new ArrayList<>(0);

    /**
     * A mapping of attribute names to their friendly names, defined globally.
     * Example might be {@code urn:oid:1.3.6.1.4.1.5923.1.1.1.6->eduPersonPrincipalName}.
     */
    private List<String> attributeFriendlyNames = new ArrayList<>(0);

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
