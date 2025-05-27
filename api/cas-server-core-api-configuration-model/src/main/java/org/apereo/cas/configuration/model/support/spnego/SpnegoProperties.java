package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SpnegoProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-spnego-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SpnegoProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8084143496524446970L;

    /**
     * Spnego settings that apply as system properties.
     */
    @NestedConfigurationProperty
    private final SpnegoSystemProperties system = new SpnegoSystemProperties();

    /**
     * Individual authentication settings for spengo that are grouped
     * and fed to the spnego authentication object to form a collection.
     */
    private final List<SpnegoAuthenticationProperties> properties = new ArrayList<>();

    /**
     * If specified, will create the principal by this name on successful authentication.
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
    @NestedConfigurationProperty
    private SpnegoLdapProperties ldap = new SpnegoLdapProperties();

    /**
     * When validating clients, specifies the DNS timeout used to look up an address.
     */
    @DurationCapable
    private String dnsTimeout = "PT2S";

    /**
     * A regex pattern that indicates whether the client host name is allowed for spnego.
     */
    @RegularExpressionCapable
    private String hostNamePatternString = ".+";

    /**
     * A regex pattern that indicates whether the client IP is allowed for spnego.
     */
    @RegularExpressionCapable
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
     * If true, does not terminate authentication and allows CAS to resume
     * and fallback to normal authentication means such as uid/psw via the login page.
     * If disallowed, considers spnego authentication to be final in the event of failures.
     */
    private boolean mixedModeAuthentication;

    /**
     * Begins negotiating spnego if the user-agent is one of the supported browsers.
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

    /**
     * The order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * The webflow configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(90);

    /**
     * The size of the pool used to validate SPNEGO tokens.
     * A pool is used to provider better performance than what was previously offered by the simple Lombok {@code Synchronized} annotation.
     */
    private int poolSize = 10;

    /**
     * The timeout of the pool used to validate SPNEGO tokens.
     */
    @DurationCapable
    private String poolTimeout = "PT2S";
}
