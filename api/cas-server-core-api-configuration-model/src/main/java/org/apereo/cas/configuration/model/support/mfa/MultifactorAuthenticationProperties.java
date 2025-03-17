package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.configuration.model.support.mfa.twilio.CasTwilioMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.yubikey.YubiKeyMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
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
public class MultifactorAuthenticationProperties implements Serializable {

    @Serial
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
    private GoogleAuthenticatorMultifactorProperties gauth = new GoogleAuthenticatorMultifactorProperties();

    /**
     * Activate and configure a multifactor authentication provider via CAS itself.
     */
    @NestedConfigurationProperty
    private CasSimpleMultifactorAuthenticationProperties simple = new CasSimpleMultifactorAuthenticationProperties();

    /**
     * Activate and configure a multifactor authentication provider via Twilio.
     */
    @NestedConfigurationProperty
    private CasTwilioMultifactorAuthenticationProperties twilio = new CasTwilioMultifactorAuthenticationProperties();

    
    /**
     * Activate and configure a multifactor authentication provider via Duo Security.
     */
    private List<DuoSecurityMultifactorAuthenticationProperties> duo = new ArrayList<>();

    /**
     * Activate and configure a multifactor authentication provider via Inwebo.
     */
    @NestedConfigurationProperty
    private InweboMultifactorAuthenticationProperties inwebo = new InweboMultifactorAuthenticationProperties();

}
