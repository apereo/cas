package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcClientRegistrationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcClientRegistrationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 123128615694269276L;

    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private DynamicClientRegistrationModes dynamicClientRegistrationMode = DynamicClientRegistrationModes.PROTECTED;

    /**
     * When client secret is issued by CAS, this is the period
     * that gets added to the current time measured in UTC to determine
     * the client secret's expiration date. An example value would be {@code P14D}
     * forcing client applications to expire their client secret in 2 weeks after the
     * registration date. Expired client secrets can be updated using the client configuration
     * endpoint. A value of {@code 0} indicates that client secrets would never expire.
     */
    @DurationCapable
    private String clientSecretExpiration = "0";

    /**
     * The username used in a basic-auth scheme to request an initial access token
     * that would then be used to dynamically register clients
     * in  {@link DynamicClientRegistrationModes#PROTECTED} mode.
     */
    private String initialAccessTokenUser;

    /**
     * Whether dynamic client registration is enabled or not.
     */
    private boolean dynamicClientRegistrationEnabled = true;
    
    /**
     * The password used in a basic-auth scheme to request an initial access token
     * that would then be used to dynamically register clients
     * in {@link DynamicClientRegistrationModes#PROTECTED} mode.
     */
    private String initialAccessTokenPassword;

    /**
     * Dynamic client registration mode.
     */
    public enum DynamicClientRegistrationModes {

        /**
         * Registration is open to all.
         * In a situation where CAS is supporting open Client registration,
         * it will check to see if the {@code logo_uri} and {@code policy_uri} have the same host
         * as the hosts defined in the array of {@code redirect_uris}.
         */
        OPEN,
        /**
         * registration is protected for all.
         */
        PROTECTED;

        /**
         * Is protected?
         *
         * @return true/false
         */
        public boolean isProtected() {
            return this == DynamicClientRegistrationModes.PROTECTED;
        }
    }
}
