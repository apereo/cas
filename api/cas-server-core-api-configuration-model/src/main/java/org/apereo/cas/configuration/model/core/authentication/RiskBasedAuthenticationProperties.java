package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link RiskBasedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-electrofence", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RiskBasedAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 3826749727400569308L;

    /**
     * Handle risky authentication attempts via an IP criteria.
     */
    private IpAddress ip = new IpAddress();

    /**
     * Handle risky authentication attempts via a user-agent criteria.
     */
    private Agent agent = new Agent();

    /**
     * Handle risky authentication attempts via geolocation criteria.
     */
    private GeoLocation geoLocation = new GeoLocation();

    /**
     * Handle risky authentication attempts via an date/time criteria.
     */
    private DateTime dateTime = new DateTime();

    /**
     * Design how responses should be handled, in the event
     * that an authentication event is deemed risky.
     */
    private Response response = new Response();

    /**
     * The risk threshold factor beyond which the authentication
     * event may be considered risky.
     */
    private double threshold = 0.6;

    /**
     * Indicates how far back the search in authentication history must go
     * in order to locate authentication events.
     */
    private long daysInRecentHistory = 30;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class IpAddress implements Serializable {

        private static final long serialVersionUID = 577801361041617794L;

        /**
         * Enable IP address checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class Agent implements Serializable {

        private static final long serialVersionUID = 7766080681971729400L;

        /**
         * Enable user-agent checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class GeoLocation implements Serializable {

        private static final long serialVersionUID = 4115333388680538358L;

        /**
         * Enable geolocation checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class DateTime implements Serializable {

        private static final long serialVersionUID = -3776875583039922050L;

        /**
         * Enable date/time checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;

        /**
         * The hourly window used before and after each authentication event
         * in calculation to establish a pattern that can then be compared against the threshold.
         */
        private int windowInHours = 2;
    }

    @Getter
    @Setter
    public static class Response implements Serializable {

        private static final long serialVersionUID = 8254082561120701582L;

        /**
         * If an authentication attempt is deemed risky, block the response
         * and do not allow further attempts.
         */
        private boolean blockAttempt;

        /**
         * If an authentication attempt is deemed risky, force
         * a multi-factor authentication event noted by the provider id here.
         */
        private String mfaProvider;

        /**
         * If an authentication attempt is deemed risky, communicate the nature of
         * this attempt back to the application via a special attribute
         * in the final CAS response indicated here.
         */
        private String riskyAuthenticationAttribute = "triggeredRiskBasedAuthentication";

        /**
         * Email settings for notifications,
         * If an authentication attempt is deemed risky.
         */
        @NestedConfigurationProperty
        private EmailProperties mail = new EmailProperties();

        /**
         * SMS settings for notifications,
         * If an authentication attempt is deemed risky.
         */
        @NestedConfigurationProperty
        private SmsProperties sms = new SmsProperties();
    }
}
