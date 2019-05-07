package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AccepttoMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-acceptto-mfa")
@Getter
@Setter
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
     * users select “Accept” or “Decline” with the push, or verifies
     * with a one-time passcode, they will get redirected back to
     * callback url that was passed by the relying party.
     */
    @RequiredProperty
    private String authnSelectionUrl = "https://mfa.acceptto.com/mfa/index";

    /**
     * Base URL for API calls  to authenticate, fetch channels or verify responses.
     */
    @RequiredProperty
    private String apiUrl = "https://mfa.acceptto.com/api/v9/";

    /**
     * uid of the application. When an organization creates
     * an application in eGuardian® dashboard this uid gets generated.
     */
    @RequiredProperty
    private String applicationId;

    /**
     * secret of the application. When an organization creates an
     * application in eGuardian® dashboard this secret gets generated.
     */
    @RequiredProperty
    private String secret;

    /**
     * message to deliver to the user. This message gets delivered to
     * the user device via push notification. e.g “Would you like to sign in?”
     */
    private String message = "Would you like to sign into CAS?";

    /**
     * The user attribute that collect's the user's email address
     * which the relying party wants to authenticate.
     */
    @RequiredProperty
    private String emailAttribute = "mail";

    /**
     * Timeout value for the authentication request is in seconds.
     * If the user doesn’t respond in the specified time period, an authentication
     * request expires. The max value is 600 seconds. Setting the value any
     * higher will cause it to revert back to 600 seconds.
     */
    private long timeout = 30;

    public AccepttoMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
