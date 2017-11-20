package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

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
public class MultifactorAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 7416521468929733907L;

    /**
     * Attribute returned in the final CAS validation payload
     * that indicates the authentication context class satisified
     * in the event of a multifactor authentication attempt.
     */
    private String authenticationContextAttribute = "authnContextClass";
    /**
     * Defines the global failure mode for the entire deployment.
     * This is meant to be used a shortcut to define the policy globally
     * rather than per application. Applications registered with CAS can still
     * define a failure mode and override the global.
     */
    private String globalFailureMode = "CLOSED";
    /**
     * MFA can be triggered for a specific authentication request,
     * provided the initial request to the CAS /login endpoint contains a parameter that indicates the required MFA authentication flow.
     * The parameter name is configurable, but its value must match the authentication provider id of an available MFA provider.
     */
    private String requestParameter = "authn_method";

    /**
     * MFA can be triggered based on the results of a remote REST endpoint of your design.
     * If the endpoint is configured, CAS shall issue a POST, providing the principal and the service url.
     * The body of the response in the event of a successful 200 status code is
     * expected to be the MFA provider id which CAS should activate.
     */
    private String restEndpoint;

    /**
     * MFA can be triggered based on the results of a groovy script of your own design.
     * The outcome of the script should determine the MFA provider id that CAS should attempt to activate.
     */
    private Resource groovyScript;

    /**
     * This is a more generic variant of the @{link #globalPrincipalAttributeNameTriggers}.
     * It may be useful in cases where there
     * is more than one provider configured and available in the application runtime and
     * you need to design a strategy to dynamically decide on the provider that should be activated for the request.
     * The decision is handed off to a Predicate implementation that define in a Groovy script whose location is taught to CAS.
     */
    private Resource globalPrincipalAttributePredicate;
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
     * Content-type that is expected to be specified by non-web clients such as curl, etc in the
     * event that the provider supports variations of non-browser based MFA.
     */
    private String contentType = "application/cas";
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
     * In the event that multiple multifactor authentication providers are determined for a multifactor authentication transaction,
     * by default CAS will attempt to sort the collection of providers based on their rank and
     * will pick one with the highest priority. This use case may arise if multiple triggers
     * are defined where each decides on a different multifactor authentication provider, or
     * the same provider instance is configured multiple times with many instances.
     * Provider selection may also be carried out using Groovy scripting strategies more dynamically.
     * The following example should serve as an outline of how to select multifactor providers based on a Groovy script.
     */
    private Resource providerSelectorGroovyScript;

    /**
     * Activate and configure a multifactor authentication provider via U2F FIDO.
     */
    @NestedConfigurationProperty
    private U2FMultifactorProperties u2f = new U2FMultifactorProperties();

    /**
     * Activate and configure a multifactor authentication provider via Microsoft Azure.
     */
    @NestedConfigurationProperty
    private AzureMultifactorProperties azure = new AzureMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication with the capability to trust and remember devices.
     */
    @NestedConfigurationProperty
    private TrustedDevicesMultifactorProperties trusted = new TrustedDevicesMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication provider via YubiKey.
     */
    @NestedConfigurationProperty
    private YubiKeyMultifactorProperties yubikey = new YubiKeyMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication provider via RADIUS.
     */
    @NestedConfigurationProperty
    private RadiusMultifactorProperties radius = new RadiusMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication provider via Google Authenticator.
     */
    @NestedConfigurationProperty
    private GAuthMultifactorProperties gauth = new GAuthMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication provider via Duo Security.
     */
    private List<DuoSecurityMultifactorProperties> duo = new ArrayList<>();
    /**
     * Activate and configure a multifactor authentication provider via Authy.
     */
    @NestedConfigurationProperty
    private AuthyMultifactorProperties authy = new AuthyMultifactorProperties();
    /**
     * Activate and configure a multifactor authentication provider via Swivel.
     */
    @NestedConfigurationProperty
    private SwivelMultifactorProperties swivel = new SwivelMultifactorProperties();

    public Resource getGlobalPrincipalAttributePredicate() {
        return globalPrincipalAttributePredicate;
    }

    public void setGlobalPrincipalAttributePredicate(final Resource globalPrincipalAttributePredicate) {
        this.globalPrincipalAttributePredicate = globalPrincipalAttributePredicate;
    }

    public Resource getProviderSelectorGroovyScript() {
        return providerSelectorGroovyScript;
    }

    public void setProviderSelectorGroovyScript(final Resource providerSelectorGroovyScript) {
        this.providerSelectorGroovyScript = providerSelectorGroovyScript;
    }

    public Resource getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(final Resource groovyScript) {
        this.groovyScript = groovyScript;
    }

    public SwivelMultifactorProperties getSwivel() {
        return swivel;
    }

    public void setSwivel(final SwivelMultifactorProperties swivel) {
        this.swivel = swivel;
    }

    public U2FMultifactorProperties getU2f() {
        return u2f;
    }

    public void setU2f(final U2FMultifactorProperties u2f) {
        this.u2f = u2f;
    }

    public AzureMultifactorProperties getAzure() {
        return azure;
    }

    public void setAzure(final AzureMultifactorProperties azure) {
        this.azure = azure;
    }

    public TrustedDevicesMultifactorProperties getTrusted() {
        return trusted;
    }

    public void setTrusted(final TrustedDevicesMultifactorProperties trusted) {
        this.trusted = trusted;
    }

    public AuthyMultifactorProperties getAuthy() {
        return authy;
    }

    public void setAuthy(final AuthyMultifactorProperties authy) {
        this.authy = authy;
    }

    public String getRestEndpoint() {
        return restEndpoint;
    }

    public void setRestEndpoint(final String restEndpoint) {
        this.restEndpoint = restEndpoint;
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(final String requestParameter) {
        this.requestParameter = requestParameter;
    }

    public String getGlobalAuthenticationAttributeNameTriggers() {
        return globalAuthenticationAttributeNameTriggers;
    }

    public void setGlobalAuthenticationAttributeNameTriggers(final String globalAuthenticationAttributeNameTriggers) {
        this.globalAuthenticationAttributeNameTriggers = globalAuthenticationAttributeNameTriggers;
    }

    public String getGlobalAuthenticationAttributeValueRegex() {
        return globalAuthenticationAttributeValueRegex;
    }

    public void setGlobalAuthenticationAttributeValueRegex(final String globalAuthenticationAttributeValueRegex) {
        this.globalAuthenticationAttributeValueRegex = globalAuthenticationAttributeValueRegex;
    }

    public String getGlobalPrincipalAttributeValueRegex() {
        return globalPrincipalAttributeValueRegex;
    }

    public void setGlobalPrincipalAttributeValueRegex(final String globalPrincipalAttributeValueRegex) {
        this.globalPrincipalAttributeValueRegex = globalPrincipalAttributeValueRegex;
    }

    public String getGlobalPrincipalAttributeNameTriggers() {
        return globalPrincipalAttributeNameTriggers;
    }

    public void setGlobalPrincipalAttributeNameTriggers(final String globalPrincipalAttributeNameTriggers) {
        this.globalPrincipalAttributeNameTriggers = globalPrincipalAttributeNameTriggers;
    }

    public String getGrouperGroupField() {
        return grouperGroupField;
    }

    public void setGrouperGroupField(final String grouperGroupField) {
        this.grouperGroupField = grouperGroupField;
    }

    public List<DuoSecurityMultifactorProperties> getDuo() {
        return duo;
    }

    public void setDuo(final List<DuoSecurityMultifactorProperties> duo) {
        this.duo = duo;
    }

    public GAuthMultifactorProperties getGauth() {
        return gauth;
    }

    public void setGauth(final GAuthMultifactorProperties gauth) {
        this.gauth = gauth;
    }

    public RadiusMultifactorProperties getRadius() {
        return radius;
    }

    public void setRadius(final RadiusMultifactorProperties radius) {
        this.radius = radius;
    }

    public String getGlobalFailureMode() {
        return globalFailureMode;
    }

    public void setGlobalFailureMode(final String globalFailureMode) {
        this.globalFailureMode = globalFailureMode;
    }

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getGlobalProviderId() {
        return globalProviderId;
    }

    public void setGlobalProviderId(final String globalProviderId) {
        this.globalProviderId = globalProviderId;
    }

    public YubiKeyMultifactorProperties getYubikey() {
        return yubikey;
    }

    public void setYubikey(final YubiKeyMultifactorProperties yubikey) {
        this.yubikey = yubikey;
    }
}
