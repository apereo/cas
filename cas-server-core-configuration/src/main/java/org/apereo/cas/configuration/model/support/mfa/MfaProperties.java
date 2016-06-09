package org.apereo.cas.configuration.model.support.mfa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for cas.mfa.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.mfa", ignoreUnknownFields = false)
public class MfaProperties {

    private String authenticationContextAttribute = "authnContextClass";
    private String globalFailureMode = "CLOSED";
    private String requestParameter = "authn_method";
    private String principalAttributes;
    
    private YubiKey yubikey = new YubiKey();
    private Radius radius = new Radius();
    private GAuth gauth = new GAuth();
    private Duo duo = new Duo();

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(final String requestParameter) {
        this.requestParameter = requestParameter;
    }

    public String getPrincipalAttributes() {
        return principalAttributes;
    }

    public void setPrincipalAttributes(final String principalAttributes) {
        this.principalAttributes = principalAttributes;
    }

    public Duo getDuo() {
        return duo;
    }

    public void setDuo(final Duo duo) {
        this.duo = duo;
    }

    public GAuth getGauth() {
        return gauth;
    }

    public void setGauth(final GAuth gauth) {
        this.gauth = gauth;
    }

    public Radius getRadius() {
        return radius;
    }

    public void setRadius(final Radius radius) {
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


    public YubiKey getYubikey() {
        return yubikey;
    }

    public void setYubikey(final YubiKey yubikey) {
        this.yubikey = yubikey;
    }

    public static class YubiKey {
        private Integer clientId;
        private String secretKey = "";
        private int rank;

        public Integer getClientId() {
            return clientId;
        }

        public void setClientId(final Integer clientId) {
            this.clientId = clientId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(final String secretKey) {
            this.secretKey = secretKey;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(final int rank) {
            this.rank = rank;
        }
    }

    public static class Radius {
        private int rank;
        
        private boolean failoverOnException;
        private boolean failoverOnAuthenticationFailure;
        
        private Server server = new Server();
        private Client client = new Client();

        public boolean isFailoverOnException() {
            return failoverOnException;
        }

        public void setFailoverOnException(final boolean failoverOnException) {
            this.failoverOnException = failoverOnException;
        }

        public boolean isFailoverOnAuthenticationFailure() {
            return failoverOnAuthenticationFailure;
        }

        public void setFailoverOnAuthenticationFailure(final boolean failoverOnAuthenticationFailure) {
            this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(final int rank) {
            this.rank = rank;
        }

        public Server getServer() {
            return server;
        }

        public void setServer(final Server server) {
            this.server = server;
        }

        public Client getClient() {
            return client;
        }

        public void setClient(final Client client) {
            this.client = client;
        }

        public static class Server {
            private String protocol = "EAP_MSCHAPv2";
            private int retries = 3;
            private long nasIdentifier = -1;
            private long nasPort = -1;
            private long nasPortId = -1;
            private long nasRealPort = -1;
            private int nasPortType = -1;
            private String nasIpAddress;
            private String nasIpv6Address;

            public String getProtocol() {
                return protocol;
            }

            public void setProtocol(final String protocol) {
                this.protocol = protocol;
            }

            public int getRetries() {
                return retries;
            }

            public void setRetries(final int retries) {
                this.retries = retries;
            }

            public long getNasIdentifier() {
                return nasIdentifier;
            }

            public void setNasIdentifier(final long nasIdentifier) {
                this.nasIdentifier = nasIdentifier;
            }

            public long getNasPort() {
                return nasPort;
            }

            public void setNasPort(final long nasPort) {
                this.nasPort = nasPort;
            }

            public long getNasPortId() {
                return nasPortId;
            }

            public void setNasPortId(final long nasPortId) {
                this.nasPortId = nasPortId;
            }

            public long getNasRealPort() {
                return nasRealPort;
            }

            public void setNasRealPort(final long nasRealPort) {
                this.nasRealPort = nasRealPort;
            }

            public int getNasPortType() {
                return nasPortType;
            }

            public void setNasPortType(final int nasPortType) {
                this.nasPortType = nasPortType;
            }

            public String getNasIpAddress() {
                return nasIpAddress;
            }

            public void setNasIpAddress(final String nasIpAddress) {
                this.nasIpAddress = nasIpAddress;
            }

            public String getNasIpv6Address() {
                return nasIpv6Address;
            }

            public void setNasIpv6Address(final String nasIpv6Address) {
                this.nasIpv6Address = nasIpv6Address;
            }


        }

        public static class Client {
            private String inetAddress = "localhost";
            private String sharedSecret = "N0Sh@ar3d$ecReT";
            private int socketTimeout;
            private int authenticationPort = 1812;
            private int accountingPort = 1813;

            public String getSharedSecret() {
                return sharedSecret;
            }

            public void setSharedSecret(final String sharedSecret) {
                this.sharedSecret = sharedSecret;
            }

            public int getSocketTimeout() {
                return socketTimeout;
            }

            public void setSocketTimeout(final int socketTimeout) {
                this.socketTimeout = socketTimeout;
            }

            public int getAuthenticationPort() {
                return authenticationPort;
            }

            public void setAuthenticationPort(final int authenticationPort) {
                this.authenticationPort = authenticationPort;
            }

            public int getAccountingPort() {
                return accountingPort;
            }

            public void setAccountingPort(final int accountingPort) {
                this.accountingPort = accountingPort;
            }

            public String getInetAddress() {
                return inetAddress;
            }

            public void setInetAddress(final String inetAddress) {
                this.inetAddress = inetAddress;
            }
        }
    }

    public static class Duo {
        private int rank;
        private String duoIntegrationKey;
        private String duoSecretKey;
        private String duoApplicationKey;
        private String duoApiHost;

        public int getRank() {
            return rank;
        }

        public void setRank(final int rank) {
            this.rank = rank;
        }

        public String getDuoIntegrationKey() {
            return duoIntegrationKey;
        }

        public void setDuoIntegrationKey(final String duoIntegrationKey) {
            this.duoIntegrationKey = duoIntegrationKey;
        }

        public String getDuoSecretKey() {
            return duoSecretKey;
        }

        public void setDuoSecretKey(final String duoSecretKey) {
            this.duoSecretKey = duoSecretKey;
        }

        public String getDuoApplicationKey() {
            return duoApplicationKey;
        }

        public void setDuoApplicationKey(final String duoApplicationKey) {
            this.duoApplicationKey = duoApplicationKey;
        }

        public String getDuoApiHost() {
            return duoApiHost;
        }

        public void setDuoApiHost(final String duoApiHost) {
            this.duoApiHost = duoApiHost;
        }
    }

    public static class GAuth {
        private String issuer;
        private String label;
        private int rank;

        private int codeDigits = 6;
        private long timeStepSize = 30;
        private int windowSize = 3;

        public int getRank() {
            return rank;
        }

        public void setRank(final int rank) {
            this.rank = rank;
        }

        public int getCodeDigits() {
            return codeDigits;
        }

        public void setCodeDigits(final int codeDigits) {
            this.codeDigits = codeDigits;
        }

        public long getTimeStepSize() {
            return timeStepSize;
        }

        public void setTimeStepSize(final long timeStepSize) {
            this.timeStepSize = timeStepSize;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(final int windowSize) {
            this.windowSize = windowSize;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(final String issuer) {
            this.issuer = issuer;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }
}
