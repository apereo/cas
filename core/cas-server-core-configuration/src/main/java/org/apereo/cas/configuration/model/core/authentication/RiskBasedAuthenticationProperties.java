package org.apereo.cas.configuration.model.core.authentication;

/**
 * This is {@link RiskBasedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskBasedAuthenticationProperties {

    private IpAddress ip = new IpAddress();
    private Agent agent = new Agent();
    private GeoLocation geoLocation = new GeoLocation();
    private DateTime dateTime = new DateTime();
    private Response response = new Response();
    
    private double threshold = 0.6;
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

    public static class IpAddress {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Agent {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class GeoLocation {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class DateTime {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Response {
        private boolean blockAttempt;
        private String mfaProvider;

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
    }
}
