package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link BaseMultifactorAuthenticationProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-authentication-mfa")
public abstract class BaseMultifactorAuthenticationProviderProperties implements Serializable {

    private static final long serialVersionUID = -2690281104343633871L;

    /**
     * At times, CAS needs to determine the correct provider when step-up authentication is required.
     * Consider for a moment that CAS already has established an SSO session with/without a provider and has
     * reached a level of authentication. Another incoming request attempts to exercise that SSO
     * session with a different and often competing authentication requirement that may differ from the
     * authentication level CAS has already established. Concretely, examples may be:
     * <ul>
     * <li>CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity.</li>
     * <li>CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity,
     * but a separate request now requires step-up authentication with YubiKey. </li>
     * </ul>
     * In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other.
     * If CAS already has achieved a level that is higher than what the incoming request requires,
     * no step-up authentication will be performed. If the opposite is true, CAS will route the authentication
     * flow to the required authentication level and upon success, will adjust the SSO session with the
     * new higher authentication level now satisfied.
     * Ranking of authentication methods is done per provider via specific properties for each.
     * Note that the higher the rank value is, the higher on the security scale it remains.
     * A provider that ranks higher with a larger weight value trumps and override others with a lower value.
     */
    private int rank;

    /**
     * The order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * The identifier for the multifactor provider.
     * In most cases, this need not be configured explicitly, unless
     * multiple instances of the same provider type are configured in CAS.
     */
    private String id;

    /**
     * Multifactor bypass options for this provider.
     * Each multifactor provider is equipped with options to allow for MFA bypass. Once the provider is chosen to honor
     * the authentication request, bypass rules are then consulted to calculate whether the provider
     * should ignore the request and skip MFA conditionally.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationProviderBypassProperties bypass = new MultifactorAuthenticationProviderBypassProperties();

    /**
     * The name of the authentication handler used to verify credentials in MFA.
     */
    private String name;

    /**
     * The failure mode policy for this MFA provider. The authentication policy by default
     * supports fail-closed mode, which means that if you attempt to
     * exercise a particular provider available to CAS and the provider cannot be reached, authentication
     * will be stopped and an error will be displayed. You can of course change this behavior so that authentication
     * proceeds without exercising the provider functionality, if that provider cannot respond.
     * Each defined multifactor authentication provider can set its own failure mode policy. Failure modes set at this location
     * will override the global failure mode, but defer to any failure mode set by the registered service.
     */
    private MultifactorAuthenticationProviderFailureModes failureMode = MultifactorAuthenticationProviderFailureModes.CLOSED;

    /**
     * This is {@link BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes}.
     *
     * @author Misagh Moayyed
     * @since 6.4.0
     */
    public enum MultifactorAuthenticationProviderFailureModes {
        /**
         * Disallow MFA, proceed with authentication but don't communicate MFA to the RP.
         */
        OPEN,

        /**
         * Disallow MFA, block with authentication.
         */
        CLOSED,

        /**
         * Disallow MFA, proceed with authentication and communicate MFA to the RP.
         */
        PHANTOM,

        /**
         * Do not check for failure at all.
         */
        NONE,

        /**
         * The default one indicating that no failure mode is set at all.
         */
        UNDEFINED
    }

}
