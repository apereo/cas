package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link RiskBasedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskBasedAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 3826749727400569308L;
    /**
     * Handle risky authentication attempts via an IP criteria.
     */
    private IpAddress ip = new IpAddress();

    /**
     *  Handle risky authentication attempts via a user-agent criteria.
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

    public long getDaysInRecentHistory() {
        return daysInRecentHistory;
    }

    public void setDaysInRecentHistory(final long daysInRecentHistory) {
        this.daysInRecentHistory = daysInRecentHistory;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(final Response response) {
        this.response = response;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(final double threshold) {
        this.threshold = threshold;
    }

    public IpAddress getIp() {
        return ip;
    }

    public void setIp(final IpAddress ip) {
        this.ip = ip;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(final Agent agent) {
        this.agent = agent;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(final DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public static class IpAddress implements Serializable {
        private static final long serialVersionUID = 577801361041617794L;
        /**
         * Enable IP address checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Agent implements Serializable {

        private static final long serialVersionUID = 7766080681971729400L;
        /**
         * Enable user-agent checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class GeoLocation implements Serializable {

        private static final long serialVersionUID = 4115333388680538358L;
        /**
         * Enable geolocation checking and criteria
         * to calculate risky authentication attempts.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

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

        public int getWindowInHours() {
            return windowInHours;
        }

        public void setWindowInHours(final int windowInHours) {
            this.windowInHours = windowInHours;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
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
        private Mail mail = new Mail();

        @NestedConfigurationProperty
        private SmsProperties sms = new SmsProperties();
        
        public SmsProperties getSms() {
            return sms;
        }

        public void setSms(final SmsProperties sms) {
            this.sms = sms;
        }

        public Mail getMail() {
            return mail;
        }

        public void setMail(final Mail mail) {
            this.mail = mail;
        }

        public boolean isBlockAttempt() {
            return blockAttempt;
        }

        public void setBlockAttempt(final boolean blockAttempt) {
            this.blockAttempt = blockAttempt;
        }

        public String getMfaProvider() {
            return mfaProvider;
        }

        public void setMfaProvider(final String mfaProvider) {
            this.mfaProvider = mfaProvider;
        }

        public String getRiskyAuthenticationAttribute() {
            return riskyAuthenticationAttribute;
        }

        public void setRiskyAuthenticationAttribute(final String riskyAuthenticationAttribute) {
            this.riskyAuthenticationAttribute = riskyAuthenticationAttribute;
        }

        public static class Mail implements Serializable {

            private static final long serialVersionUID = 7367120636536230761L;
            /**
             * Principal attribute name that indicates the destination email address
             * for this message. The attribute must already be resolved and available
             * to the CAS principal.
             */
            private String attributeName = "mail";

            /**
             * Email message body.
             */
            private String text;

            /**
             * Email from address.
             */
            private String from;

            /**
             * Email subject line.
             */
            private String subject;

            /**
             * Email CC address, if any.
             */
            private String cc;

            /**
             * Email BCC address, if any.
             */
            private String bcc;

            public String getAttributeName() {
                return attributeName;
            }

            public void setAttributeName(final String attributeName) {
                this.attributeName = attributeName;
            }

            public String getBcc() {
                return bcc;
            }

            public void setBcc(final String bcc) {
                this.bcc = bcc;
            }

            public String getText() {
                return text;
            }

            public void setText(final String text) {
                this.text = text;
            }

            public String getFrom() {
                return from;
            }

            public void setFrom(final String from) {
                this.from = from;
            }

            public String getSubject() {
                return subject;
            }

            public void setSubject(final String subject) {
                this.subject = subject;
            }

            public String getCc() {
                return cc;
            }

            public void setCc(final String cc) {
                this.cc = cc;
            }
        }
    }
}
