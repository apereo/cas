package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration properties class for cas.mfa.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class MultifactorAuthenticationProperties {

    private String authenticationContextAttribute = "authnContextClass";
    private String globalFailureMode = "CLOSED";
    private String requestParameter = "authn_method";
    
    private String restEndpoint;
    
    private String globalPrincipalAttributeNameTriggers;
    private String globalPrincipalAttributeValueRegex;
    
    private String contentType = "application/cas";
    private String globalProviderId;
    
    private Trusted trusted = new Trusted();
    private YubiKey yubikey = new YubiKey();
    private Radius radius = new Radius();
    private GAuth gauth = new GAuth();
    private Duo duo = new Duo();
    private Authy authy = new Authy();

    public Trusted getTrusted() {
        return trusted;
    }

    public void setTrusted(final Trusted trusted) {
        this.trusted = trusted;
    }

    public Authy getAuthy() {
        return authy;
    }

    public void setAuthy(final Authy authy) {
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

    public YubiKey getYubikey() {
        return yubikey;
    }

    public void setYubikey(final YubiKey yubikey) {
        this.yubikey = yubikey;
    }

    public abstract static class BaseProvider {
        private Bypass bypass = new Bypass();

        public Bypass getBypass() {
            return bypass;
        }

        public void setBypass(final Bypass bypass) {
            this.bypass = bypass;
        }

        public static class Bypass {
            private String principalAttributeName;
            private String principalAttributeValue;
            private String authenticationAttributeName;
            private String authenticationAttributeValue;
            private String authenticationHandlerName;
            private String authenticationMethodName;
            private String credentialClassType;

            public String getCredentialClassType() {
                return credentialClassType;
            }

            public void setCredentialClassType(final String credentialClassType) {
                this.credentialClassType = credentialClassType;
            }

            public String getAuthenticationAttributeName() {
                return authenticationAttributeName;
            }

            public void setAuthenticationAttributeName(final String authenticationAttributeName) {
                this.authenticationAttributeName = authenticationAttributeName;
            }

            public String getAuthenticationAttributeValue() {
                return authenticationAttributeValue;
            }

            public void setAuthenticationAttributeValue(final String authenticationAttributeValue) {
                this.authenticationAttributeValue = authenticationAttributeValue;
            }

            public String getPrincipalAttributeName() {
                return principalAttributeName;
            }

            public void setPrincipalAttributeName(final String principalAttributeName) {
                this.principalAttributeName = principalAttributeName;
            }

            public String getPrincipalAttributeValue() {
                return principalAttributeValue;
            }

            public void setPrincipalAttributeValue(final String principalAttributeValue) {
                this.principalAttributeValue = principalAttributeValue;
            }

            public String getAuthenticationHandlerName() {
                return authenticationHandlerName;
            }

            public void setAuthenticationHandlerName(final String authenticationHandlerName) {
                this.authenticationHandlerName = authenticationHandlerName;
            }

            public String getAuthenticationMethodName() {
                return authenticationMethodName;
            }

            public void setAuthenticationMethodName(final String authenticationMethodName) {
                this.authenticationMethodName = authenticationMethodName;
            }
        }
    }
    
    public static class YubiKey extends BaseProvider {
        private Integer clientId;
        private String secretKey = "";
        private int rank;
        private List<String> apiUrls = new ArrayList<>();
        private boolean trustedDeviceEnabled = true;

        public boolean isTrustedDeviceEnabled() {
            return trustedDeviceEnabled;
        }

        public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
            this.trustedDeviceEnabled = trustedDeviceEnabled;
        }

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

        public List<String> getApiUrls() {
            return apiUrls;
        }

        public void setApiUrls(final List<String> apiUrls) {
            this.apiUrls = apiUrls;
        }
    }

    public static class Radius extends BaseProvider {
        private int rank;

        private boolean failoverOnException;
        private boolean failoverOnAuthenticationFailure;

        private Server server = new Server();
        private Client client = new Client();

        private boolean trustedDeviceEnabled = true;

        public boolean isTrustedDeviceEnabled() {
            return trustedDeviceEnabled;
        }

        public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
            this.trustedDeviceEnabled = trustedDeviceEnabled;
        }
        
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
            private String nasIdentifier;
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

            public String getNasIdentifier() {
                return nasIdentifier;
            }

            public void setNasIdentifier(final String nasIdentifier) {
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

    public static class Duo extends BaseProvider {
        private int rank;
        private String duoIntegrationKey;
        private String duoSecretKey;
        private String duoApplicationKey;
        private String duoApiHost;
        private boolean trustedDeviceEnabled = true;

        public boolean isTrustedDeviceEnabled() {
            return trustedDeviceEnabled;
        }

        public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
            this.trustedDeviceEnabled = trustedDeviceEnabled;
        }
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

    public static class Authy extends BaseProvider {
        private String apiKey;
        private String apiUrl;
        private String phoneAttribute = "phone";
        private String mailAttribute = "mail";
        private boolean forceVerification = true;
        private boolean trustedDeviceEnabled = true;

        public boolean isTrustedDeviceEnabled() {
            return trustedDeviceEnabled;
        }

        public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
            this.trustedDeviceEnabled = trustedDeviceEnabled;
        }
        
        public String getPhoneAttribute() {
            return phoneAttribute;
        }

        public void setPhoneAttribute(final String phoneAttribute) {
            this.phoneAttribute = phoneAttribute;
        }

        public String getMailAttribute() {
            return mailAttribute;
        }

        public void setMailAttribute(final String mailAttribute) {
            this.mailAttribute = mailAttribute;
        }

        public boolean isForceVerification() {
            return forceVerification;
        }

        public void setForceVerification(final boolean forceVerification) {
            this.forceVerification = forceVerification;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(final String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(final String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }

    public static class Trusted extends BaseProvider {
        private String authenticationContextAttribute = "isFromTrustedMultifactorAuthentication";
        
        private String encryptionKey = "";

        private String signingKey = "";

        private boolean cipherEnabled = true;

        private boolean deviceRegistrationEnabled = true;

        private long expiration = 30;
        
        private TimeUnit timeUnit = TimeUnit.DAYS;

        private Rest rest = new Rest();
        
        private Jpa jpa = new Jpa();

        private Cleaner cleaner = new Cleaner();
        
        private Mongodb mongodb = new Mongodb();

        public Rest getRest() {
            return rest;
        }

        public void setRest(final Rest rest) {
            this.rest = rest;
        }

        public Mongodb getMongodb() {
            return mongodb;
        }

        public void setMongodb(final Mongodb mongodb) {
            this.mongodb = mongodb;
        }

        public Jpa getJpa() {
            return jpa;
        }

        public void setJpa(final Jpa jpa) {
            this.jpa = jpa;
        }

        public long getExpiration() {
            return expiration;
        }

        public void setExpiration(final long expiration) {
            this.expiration = expiration;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(final TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public String getAuthenticationContextAttribute() {
            return authenticationContextAttribute;
        }

        public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
            this.authenticationContextAttribute = authenticationContextAttribute;
        }

        public String getEncryptionKey() {
            return encryptionKey;
        }

        public void setEncryptionKey(final String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        public String getSigningKey() {
            return signingKey;
        }

        public void setSigningKey(final String signingKey) {
            this.signingKey = signingKey;
        }

        public boolean isCipherEnabled() {
            return cipherEnabled;
        }

        public void setCipherEnabled(final boolean cipherEnabled) {
            this.cipherEnabled = cipherEnabled;
        }
        
        public boolean isDeviceRegistrationEnabled() {
            return deviceRegistrationEnabled;
        }

        public void setDeviceRegistrationEnabled(final boolean deviceRegistrationEnabled) {
            this.deviceRegistrationEnabled = deviceRegistrationEnabled;
        }

        public Cleaner getCleaner() {
            return cleaner;
        }

        public void setCleaner(final Cleaner cleaner) {
            this.cleaner = cleaner;
        }

        public static class Rest {
            private String endpoint;

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(final String endpoint) {
                this.endpoint = endpoint;
            }
        }
        
        public static class Jpa extends AbstractJpaProperties {
        }

        public static class Mongodb extends AbstractMongoProperties {
            public Mongodb() {
                setCollection("MongoDbCasTrustedAuthnMfaRepository");
            }
        }
        
        public static class Cleaner {
            private boolean enabled = true;
            private long startDelay = 10000;
            private long repeatInterval = 60000;
            
            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(final boolean enabled) {
                this.enabled = enabled;
            }

            public long getStartDelay() {
                return startDelay;
            }

            public void setStartDelay(final long startDelay) {
                this.startDelay = startDelay;
            }

            public long getRepeatInterval() {
                return repeatInterval;
            }

            public void setRepeatInterval(final long repeatInterval) {
                this.repeatInterval = repeatInterval;
            }
        }
    }
    
    public static class GAuth extends BaseProvider {
        private String issuer = "CASIssuer";
        private String label = "CASLabel";
        private int rank;

        private int codeDigits = 6;
        private long timeStepSize = 30;
        private int windowSize = 3;

        private Mongodb mongodb = new Mongodb();
        
        private Jpa jpa = new Jpa();

        public Mongodb getMongodb() {
            return mongodb;
        }

        public void setMongodb(final Mongodb mongodb) {
            this.mongodb = mongodb;
        }

        public Jpa getJpa() {
            return jpa;
        }

        public void setJpa(final Jpa jpa) {
            this.jpa = jpa;
        }

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

        public static class Mongodb extends AbstractMongoProperties {
            public Mongodb() {
                setCollection("MongoDbGoogleAuthenticatorRepository");
            }
        }
        
        public static class Jpa {
            private Database database = new Database();

            public Database getDatabase() {
                return database;
            }

            public void setDatabase(final Database database) {
                this.database = database;
            }

            public static class Database extends AbstractJpaProperties {
                public Database() {
                    super.setUrl("jdbc:hsqldb:mem:cas-gauth");
                }
            }
        }
    }
}
