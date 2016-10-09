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

        private Mail mail = new Mail();
        private Sms sms = new Sms();

        public Sms getSms() {
            return sms;
        }

        public void setSms(final Sms sms) {
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
        
        public static class Mail {
            private String attributeName = "mail";
            private String text;
            private String from;
            private String subject;
            private String cc;
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
        
        public static class Sms {
            private String text;
            private String from;
            private String attributeName = "phone";
            private Twilio twilio = new Twilio();

            public String getAttributeName() {
                return attributeName;
            }

            public void setAttributeName(final String attributeName) {
                this.attributeName = attributeName;
            }

            public String getFrom() {
                return from;
            }

            public void setFrom(final String from) {
                this.from = from;
            }

            public String getText() {
                return text;
            }

            public void setText(final String text) {
                this.text = text;
            }

            public Twilio getTwilio() {
                return twilio;
            }

            public void setTwilio(final Twilio twilio) {
                this.twilio = twilio;
            }

            public static class Twilio {
                private String accountId;
                private String token;

                public String getAccountId() {
                    return accountId;
                }

                public void setAccountId(final String accountId) {
                    this.accountId = accountId;
                }

                public String getToken() {
                    return token;
                }

                public void setToken(final String token) {
                    this.token = token;
                }
            }
        }
    }
}
