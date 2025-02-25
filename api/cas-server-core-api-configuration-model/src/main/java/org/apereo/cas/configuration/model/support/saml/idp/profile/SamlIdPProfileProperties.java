package org.apereo.cas.configuration.model.support.saml.idp.profile;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SamlIdPProfileProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPProfileProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -3218075783676789852L;

    /**
     * Settings related to the saml2 sso profile.
     */
    @NestedConfigurationProperty
    private SamlIdPSSOProfileProperties sso = new SamlIdPSSOProfileProperties();

    /**
     * Settings related to the saml2 sso post simple-sign profile.
     */
    @NestedConfigurationProperty
    private SamlIdPSSOSimpleSignProfileProperties ssoPostSimpleSign = new SamlIdPSSOSimpleSignProfileProperties();

    /**
     * Settings related to the saml2 slo redirect profile.
     */
    @NestedConfigurationProperty
    private SamlIdPSLOProfileProperties slo = new SamlIdPSLOProfileProperties();

}
