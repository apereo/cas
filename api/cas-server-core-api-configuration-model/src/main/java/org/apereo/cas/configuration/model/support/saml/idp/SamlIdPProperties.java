package org.apereo.cas.configuration.model.support.saml.idp;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.model.support.saml.idp.metadata.SamlIdPMetadataProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
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
     * The scope used in generation of metadata.
     */
    @RequiredProperty
    private String scope = "example.org";

    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are references and mapped one ones that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     */
    private List<String> authenticationContextClassMappings;

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
     * Settings related to naming saml cache storages.
     */
    @NestedConfigurationProperty
    private SamlIdPTicketProperties ticket = new SamlIdPTicketProperties();

}
