package org.apereo.cas.configuration.model.support.spnego;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link SpnegoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-spnego-webflow")
@Slf4j
@Getter
@Setter
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

    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = -8835216200501334936L;
    }
}
