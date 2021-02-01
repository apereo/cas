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
     * This is a more generic variant of the {@link #globalPrincipalAttributeNameTriggers}.
     * It may be useful in cases where there
     * is more than one provider configured and available in the application runtime and
     * you need to design a strategy to dynamically decide on the provider that should be activated for the request.
     * The decision is handed off to a Predicate implementation that define in a Groovy script whose location is taught to CAS.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties globalPrincipalAttributePredicate = new SpringResourceProperties();

    /**
     * MFA can be triggered for all users/subjects carrying a specific attribute that matches one of the conditions below.
     * <ul>
     * <li>Trigger MFA based on a principal attribute(s) whose value(s) matches a regex pattern.
     * Note that this behavior is only applicable if there is only a single MFA provider configured,
     * since that would allow CAS to know what provider to next activate.</li>
     * <li>Trigger MFA based on a principal attribute(s) whose value(s) EXACTLY matches an MFA provider.
     * This option is more relevant if you have more than one provider configured or if you have the flexibility
     * of assigning provider ids to attributes as values.</li>
     * </ul>
     * Needless to say, the attributes need to have been resolved for the principal prior to this step.
     */
    private String globalPrincipalAttributeNameTriggers;

    /**
     * The regular expression that is cross matches against the principal attribute to determine
     * if the account is qualified for multifactor authentication.
     */
    private String globalPrincipalAttributeValueRegex;

    /**
     * MFA can be triggered for all users/subjects whose authentication event/metadata has resolved a specific attribute that
     * matches one of the below conditions:
     * <ul>
     * <li>Trigger MFA based on a authentication attribute(s) whose value(s) matches a regex pattern.
     * Note that this behavior is only applicable if there is only a single MFA provider configured,
     * since that would allow CAS to know what provider to next activate. </li>
     * <li>Trigger MFA based on a authentication attribute(s) whose value(s) EXACTLY matches an MFA provider.
     * This option is more relevant if you have more than one provider configured or if you have the
     * flexibility of assigning provider ids to attributes as values. </li>
     * </ul>
     * Needless to say, the attributes need to have been resolved for the authentication event prior to this step.
     * This trigger is generally useful when the underlying authentication engine signals
     * CAS to perform additional validation of credentials. This signal may be captured by CAS as
     * an attribute that is part of the authentication event metadata which can then trigger
     * additional multifactor authentication events.
     */
    private String globalAuthenticationAttributeNameTriggers;

    /**
     * The regular expression that is cross matches against the authentication attribute to determine
     * if the account is qualified for multifactor authentication.
     */
    private String globalAuthenticationAttributeValueRegex;
    
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
