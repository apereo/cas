package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.configuration.model.support.mfa.u2f.U2FMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.yubikey.YubiKeyMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.mfa.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("MultifactorAuthenticationProperties")
public class MultifactorAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 7416521468929733907L;

    /**
     * Multifactor authentication core/common settings.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationCoreProperties core = new MultifactorAuthenticationCoreProperties();

    /**
     * Multifactor authentication core/common settings for triggering mfa.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationTriggersProperties triggers = new MultifactorAuthenticationTriggersProperties();

    /**
     * MFA can be triggered based on the results of a groovy script of your own design.
     * The outcome of the script should determine the MFA provider id that CAS should attempt to activate.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovyScript = new SpringResourceProperties();

    /**
     * MFA can be triggered for all applications and users regardless of individual settings.
     * This setting holds the value of an MFA provider that shall be activated for all requests,
     * regardless.
     */
    private String globalProviderId;

    /**
     * MFA can be triggered by Grouper groups to which the authenticated principal is assigned.
     * Groups are collected by CAS and then cross-checked against all available/configured MFA providers.
     * The group’s comparing factor MUST be defined in CAS to activate this behavior and
     * it can be based on the group’s name, display name,
     * etc where a successful match against a provider id shall activate the chosen MFA provider.
     */
    private String grouperGroupField;

    /**
     * Activate and configure a multifactor authentication provider via U2F FIDO.
     */
    @NestedConfigurationProperty
    private U2FMultifactorAuthenticationProperties u2f = new U2FMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication with the capability to trust and remember devices.
     */
    @NestedConfigurationProperty
    private TrustedDevicesMultifactorProperties trusted = new TrustedDevicesMultifactorProperties();

    /**
     * Activate and configure a multifactor authentication provider via YubiKey.
     */
    @NestedConfigurationProperty
    private YubiKeyMultifactorAuthenticationProperties yubikey = new YubiKeyMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via WebAuthN.
     */
    @NestedConfigurationProperty
    private WebAuthnMultifactorAuthenticationProperties webAuthn = new WebAuthnMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via RADIUS.
     */
    @NestedConfigurationProperty
    private RadiusMultifactorAuthenticationProperties radius = new RadiusMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Google Authenticator.
     */
    @NestedConfigurationProperty
    private GoogleAuthenticatorMultifactorAuthenticationProperties gauth = new GoogleAuthenticatorMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via CAS itself.
     */
    @NestedConfigurationProperty
    private CasSimpleMultifactorAuthenticationProperties simple = new CasSimpleMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Duo Security.
     */
    private List<DuoSecurityMultifactorAuthenticationProperties> duo = new ArrayList<>(0);

    /**
     * Activate and configure a multifactor authentication provider via Authy.
     */
    @NestedConfigurationProperty
    private AuthyMultifactorAuthenticationProperties authy = new AuthyMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Swivel.
     */
    @NestedConfigurationProperty
    private SwivelMultifactorAuthenticationProperties swivel = new SwivelMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Acceptto.
     */
    @NestedConfigurationProperty
    private AccepttoMultifactorAuthenticationProperties acceptto = new AccepttoMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Inwebo.
     */
    @NestedConfigurationProperty
    private InweboMultifactorAuthenticationProperties inwebo = new InweboMultifactorAuthenticationProperties();

}
