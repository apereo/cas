package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link SpnegoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-spnego-webflow")
public class SpnegoProperties implements Serializable {

    private static final long serialVersionUID = 8084143496524446970L;
    /**
     * If specified, will create the principal by ths name on successful authentication.
     */
    private boolean principalWithDomainName;
    /**
     * Allows authentication if spnego credential is marked as NTLM.
     */
    private boolean ntlmAllowed = true;
    /**
     * If the authenticated principal cannot be determined from the spegno credential,
     * will set the http status code to 401.
     */
    private boolean send401OnAuthenticationFailure = true;

    /**
     * The bean id of a webflow action whose job is to evaluate the client host
     * to see if the request is authorized for spnego.
     * Supported strategies include {@code hostnameSpnegoClientAction} where
     * CAS checks to see if the request’s remote hostname matches a predefine pattern.
     * and {@code ldapSpnegoClientAction} where
     * CAS checks an LDAP instance for the remote hostname, to locate a pre-defined attribute whose
     * mere existence would allow the webflow to resume to SPNEGO.
     */
    private String hostNameClientActionStrategy = "hostnameSpnegoClientAction";

    /**
     * LDAP settings for spnego to validate clients, etc.
     */
    private Ldap ldap = new Ldap();

    /**
     * The Jcifs password.
     */
    private String jcifsPassword;
    /**
     * The Jcifs service password.
     */
    private String jcifsServicePassword;
    /**
     * The Jcifs service principal.
     */
    private String jcifsServicePrincipal = "HTTP/cas.example.com@EXAMPLE.COM";
    /**
     * The Kerberos conf.
     */
    private String kerberosConf;
    /**
     * The Kerberos kdc.
     */
    private String kerberosKdc = "172.10.1.10";
    /**
     * The Kerberos realm.
     */
    private String kerberosRealm = "EXAMPLE.COM";
    /**
     * The Login conf.
     */
    private String loginConf;
    /**
     * Spnego JCIFS timeout.
     */
    private String timeout = "PT5M";
    /**
     * Jcifs Netbios cache policy.
     */
    private long cachePolicy = 600;
    /**
     * The Jcifs netbios wins.
     */
    private String jcifsNetbiosWins;
    /**
     * The Jcifs username.
     */
    private String jcifsUsername;
    /**
     * The Jcifs domain controller.
     */
    private String jcifsDomainController;
    /**
     * The Jcifs domain.
     */
    private String jcifsDomain;
    /**
     * The Kerberos debug.
     */
    private String kerberosDebug;
    /**
     * The Use subject creds only.
     */
    private boolean useSubjectCredsOnly;
    /**
     * When validating clients, specifies the DNS timeout used to look up an address.
     */
    private String dnsTimeout = "PT2S";
    /**
     * A regex pattern that indicates whether the client host name is allowed for spnego.
     */
    private String hostNamePatternString = ".+";
    /**
     * A regex pattern that indicates whether the client IP is allowed for spnego.
     */
    private String ipsToCheckPattern = "127.+";
    /**
     * Alternative header name to use in order to find the host address.
     */
    private String alternativeRemoteHostAttribute = "alternateRemoteHeader";
    /**
     * In case LDAP is used to validate clients, this is the attribute that indicates the host.
     */
    private String spnegoAttributeName = "distinguishedName";

    /**
     * Determines the header to set and the message prefix when negotiating spnego.
     */
    private boolean ntlm;
    /**
     * If true, does not terminate authentication and allows CAS to resume
     * and fallback to normal authentication means such as uid/psw via the login page.
     * If disallowed, considers spnego authentication to be final in the event of failures.
     */
    private boolean mixedModeAuthentication;
    /**
     * Begins negotiating spenego if the user-agent is one of the supported browsers.
     */
    private String supportedBrowsers = "MSIE,Trident,Firefox,AppleWebKit";

    /**
     * This is principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password encoding settings for this authentication.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public PersonDirectoryPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirectoryPrincipalResolverProperties principal) {
        this.principal = principal;
    }

    public boolean isSend401OnAuthenticationFailure() {
        return send401OnAuthenticationFailure;
    }

    public void setSend401OnAuthenticationFailure(final boolean send401OnAuthenticationFailure) {
        this.send401OnAuthenticationFailure = send401OnAuthenticationFailure;
    }

    public String getHostNameClientActionStrategy() {
        return hostNameClientActionStrategy;
    }

    public void setHostNameClientActionStrategy(final String hostNameClientActionStrategy) {
        this.hostNameClientActionStrategy = hostNameClientActionStrategy;
    }

    public boolean isNtlm() {
        return ntlm;
    }

    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
    }

    public boolean isMixedModeAuthentication() {
        return mixedModeAuthentication;
    }

    public void setMixedModeAuthentication(final boolean mixedModeAuthentication) {
        this.mixedModeAuthentication = mixedModeAuthentication;
    }

    public String getSupportedBrowsers() {
        return supportedBrowsers;
    }

    public void setSupportedBrowsers(final String supportedBrowsers) {
        this.supportedBrowsers = supportedBrowsers;
    }

    public String getSpnegoAttributeName() {
        return spnegoAttributeName;
    }

    public void setSpnegoAttributeName(final String spnegoAttributeName) {
        this.spnegoAttributeName = spnegoAttributeName;
    }

    public long getDnsTimeout() {
        return Beans.newDuration(dnsTimeout).toMillis();
    }

    public void setDnsTimeout(final String dnsTimeout) {
        this.dnsTimeout = dnsTimeout;
    }

    public String getIpsToCheckPattern() {
        return ipsToCheckPattern;
    }

    public void setIpsToCheckPattern(final String ipsToCheckPattern) {
        this.ipsToCheckPattern = ipsToCheckPattern;
    }

    public String getAlternativeRemoteHostAttribute() {
        return alternativeRemoteHostAttribute;
    }

    public void setAlternativeRemoteHostAttribute(final String alternativeRemoteHostAttribute) {
        this.alternativeRemoteHostAttribute = alternativeRemoteHostAttribute;
    }

    public String getJcifsPassword() {
        return jcifsPassword;
    }

    public String getHostNamePatternString() {
        return hostNamePatternString;
    }

    public void setHostNamePatternString(final String hostNamePatternString) {
        this.hostNamePatternString = hostNamePatternString;
    }

    public void setJcifsPassword(final String jcifsPassword) {
        this.jcifsPassword = jcifsPassword;
    }

    public String getJcifsServicePassword() {
        return jcifsServicePassword;
    }

    public void setJcifsServicePassword(final String jcifsServicePassword) {
        this.jcifsServicePassword = jcifsServicePassword;
    }

    public String getJcifsServicePrincipal() {
        return jcifsServicePrincipal;
    }

    public void setJcifsServicePrincipal(final String jcifsServicePrincipal) {
        this.jcifsServicePrincipal = jcifsServicePrincipal;
    }

    public String getKerberosConf() {
        return kerberosConf;
    }

    public void setKerberosConf(final String kerberosConf) {
        this.kerberosConf = kerberosConf;
    }

    public String getKerberosKdc() {
        return kerberosKdc;
    }

    public void setKerberosKdc(final String kerberosKdc) {
        this.kerberosKdc = kerberosKdc;
    }

    public String getKerberosRealm() {
        return kerberosRealm;
    }

    public void setKerberosRealm(final String kerberosRealm) {
        this.kerberosRealm = kerberosRealm;
    }

    public String getLoginConf() {
        return loginConf;
    }

    public void setLoginConf(final String loginConf) {
        this.loginConf = loginConf;
    }

    public long getTimeout() {
        return Beans.newDuration(timeout).toMillis();
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public long getCachePolicy() {
        return cachePolicy;
    }

    public void setCachePolicy(final long cachePolicy) {
        this.cachePolicy = cachePolicy;
    }

    public String getJcifsNetbiosWins() {
        return jcifsNetbiosWins;
    }

    public void setJcifsNetbiosWins(final String jcifsNetbiosWins) {
        this.jcifsNetbiosWins = jcifsNetbiosWins;
    }

    public String getJcifsUsername() {
        return jcifsUsername;
    }

    public void setJcifsUsername(final String jcifsUsername) {
        this.jcifsUsername = jcifsUsername;
    }

    public String getJcifsDomainController() {
        return jcifsDomainController;
    }

    public void setJcifsDomainController(final String jcifsDomainController) {
        this.jcifsDomainController = jcifsDomainController;
    }

    public String getJcifsDomain() {
        return jcifsDomain;
    }

    public void setJcifsDomain(final String jcifsDomain) {
        this.jcifsDomain = jcifsDomain;
    }

    public String getKerberosDebug() {
        return kerberosDebug;
    }

    public void setKerberosDebug(final String kerberosDebug) {
        this.kerberosDebug = kerberosDebug;
    }

    public boolean isUseSubjectCredsOnly() {
        return useSubjectCredsOnly;
    }

    public void setUseSubjectCredsOnly(final boolean useSubjectCredsOnly) {
        this.useSubjectCredsOnly = useSubjectCredsOnly;
    }

    public boolean isPrincipalWithDomainName() {
        return principalWithDomainName;
    }

    public void setPrincipalWithDomainName(final boolean principalWithDomainName) {
        this.principalWithDomainName = principalWithDomainName;
    }

    public boolean isNtlmAllowed() {
        return ntlmAllowed;
    }

    public void setNtlmAllowed(final boolean ntlmAllowed) {
        this.ntlmAllowed = ntlmAllowed;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -8835216200501334936L;
        /**
         * LDAP base dn to start the search.
         */
        private String baseDn;
        /**
         * LDAP search filter to look up hosts. Example: {@code host={host}}.
         */
        private String searchFilter;

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(final String searchFilter) {
            this.searchFilter = searchFilter;
        }
    }
}

