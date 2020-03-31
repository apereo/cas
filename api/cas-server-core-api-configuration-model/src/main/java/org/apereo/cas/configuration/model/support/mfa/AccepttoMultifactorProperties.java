package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link AccepttoMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-acceptto-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class AccepttoMultifactorProperties extends BaseMultifactorProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-acceptto";

    private static final long serialVersionUID = -2309444053833490009L;

    /**
     * URL of the Acceptto instance for authn discovery.
     * This page allows the user to choose their second-factor authentication
     * method. Based on the policies defined by relying party user
     * has the option of using Push Notification, Text Message, Voice
     * Call, TOTP for replying to the authentication request. As soon as
     * users select Accept or Decline with the push, or verifies
     * with a one-time passcode, they will get redirected back to
     * callback url that was passed by the relying party.
     */
    @RequiredProperty
    private String authnSelectionUrl = "https://mfa.acceptto.com/mfa/index";

    /**
     * Base URL for API calls to authenticate, fetch channels or verify responses.
     */
    @RequiredProperty
    private String apiUrl = "https://mfa.acceptto.com/api/v9/";

    /**
     * URL to the enrollment/registration API.
     */
    @RequiredProperty
    private String registrationApiUrl = "https://mfa.acceptto.com/api/integration/v1/mfa/authenticate";

    /**
     * Identifier of the application. When an organization creates
     * an application in eGuardian dashboard this id gets generated.
     */
    @RequiredProperty
    private String applicationId;

    /**
     * Secret of the application. When an organization creates an
     * application in eGuardian dashboard this secret gets generated.
     */
    @RequiredProperty
    private String secret;

    /**
     * Message to deliver to the user. This message gets delivered to
     * the user device via push notification. e.g "Would you like to sign in?".
     */
    private String message = "Would you like to sign into CAS?";

    /**
     * The user attribute that collects the user's email address
     * which the relying party wants to authenticate.
     */
    @RequiredProperty
    private String emailAttribute = "mail";
    
    /**
     * List of active directory group GUIDs that user is a member of.
     * This is used for Group based policies. If undefined,
     * will ignore passing the groups to Acceptto.
     */
    private String groupAttribute;

    /**
     * Timeout value for the authentication request is in seconds.
     * If the user does not respond in the specified time period, an authentication
     * request expires. The max value is 600 seconds. Setting the value any
     * higher will cause it to revert back to 600 seconds.
     */
    private long timeout = 120;

    /**
     * Whether QR Code login should be enabled.
     */
    private boolean qrLoginEnabled = true;

    /**
     * Organization identifier.
     */
    @RequiredProperty
    private String organizationId;

    /**
     * Organization secret.
     */
    @RequiredProperty
    private String organizationSecret;

    /**
     * Location of public key used to verify API responses
     * that are produced as part of device pairing and registration.
     */
    @RequiredProperty
    @NestedConfigurationProperty
    private SpringResourceProperties registrationApiPublicKey = new SpringResourceProperties();

    public AccepttoMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
