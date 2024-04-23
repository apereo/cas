package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.core.authentication.passwordsync.PasswordSynchronizationProperties;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryAuthenticationProperties;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;
import org.apereo.cas.configuration.model.support.cognito.AmazonCognitoAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.GroovyAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.JsonResourceAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RemoteAuthenticationProperties;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoDbAuthenticationProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.okta.OktaAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.passwordless.PasswordlessAuthenticationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.model.support.qr.QRAuthenticationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.redis.RedisAuthenticationProperties;
import org.apereo.cas.configuration.model.support.rest.RestAuthenticationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.model.support.saml.shibboleth.ShibbolethIdPProperties;
import org.apereo.cas.configuration.model.support.soap.SoapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.configuration.model.support.trusted.TrustedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuthenticationProperties")
public class AuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1233126985007049516L;

    /**
     * Core authentication settings.
     */
    @NestedConfigurationProperty
    private CoreAuthenticationProperties core = new CoreAuthenticationProperties();

    /**
     * Passwordless authentication settings.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationProperties passwordless = new PasswordlessAuthenticationProperties();

    /**
     * QR authentication settings.
     */
    @NestedConfigurationProperty
    private QRAuthenticationProperties qr = new QRAuthenticationProperties();

    /**
     * Passwordless sync settings.
     */
    @NestedConfigurationProperty
    private PasswordSynchronizationProperties passwordSync = new PasswordSynchronizationProperties();

    /**
     * JSON authentication settings.
     */
    @NestedConfigurationProperty
    private JsonResourceAuthenticationProperties json = new JsonResourceAuthenticationProperties();

    /**
     * Groovy authentication settings.
     */
    @NestedConfigurationProperty
    private GroovyAuthenticationProperties groovy = new GroovyAuthenticationProperties();

    /**
     * Syncope authentication settings.
     */
    @NestedConfigurationProperty
    private SyncopeAuthenticationProperties syncope = new SyncopeAuthenticationProperties();

    /**
     * Azure AD authentication settings.
     */
    @NestedConfigurationProperty
    private AzureActiveDirectoryAuthenticationProperties azureActiveDirectory = new AzureActiveDirectoryAuthenticationProperties();

    /**
     * Okta authentication settings.
     */
    @NestedConfigurationProperty
    private OktaAuthenticationProperties okta = new OktaAuthenticationProperties();

    /**
     * Redis authentication settings.
     */
    @NestedConfigurationProperty
    private RedisAuthenticationProperties redis = new RedisAuthenticationProperties();

    /**
     * Cassandra authentication settings.
     */
    @NestedConfigurationProperty
    private CassandraAuthenticationProperties cassandra = new CassandraAuthenticationProperties();

    /**
     * Cloud Directory authentication settings.
     */
    @NestedConfigurationProperty
    private AmazonCloudDirectoryProperties cloudDirectory = new AmazonCloudDirectoryProperties();

    /**
     * Configuration settings for cognito authentication.
     */
    @NestedConfigurationProperty
    private AmazonCognitoAuthenticationProperties cognito = new AmazonCognitoAuthenticationProperties();

    /**
     * Settings that control SOAP authentication.
     */
    @NestedConfigurationProperty
    private SoapAuthenticationProperties soap = new SoapAuthenticationProperties();

    /**
     * Surrogate authentication settings.
     */
    @NestedConfigurationProperty
    private SurrogateAuthenticationProperties surrogate = new SurrogateAuthenticationProperties();

    /**
     * Graphical User authentication settings.
     */
    @NestedConfigurationProperty
    private GraphicalUserAuthenticationProperties gua = new GraphicalUserAuthenticationProperties();

    /**
     * Password management settings.
     */
    @NestedConfigurationProperty
    private PasswordManagementProperties pm = new PasswordManagementProperties();

    /**
     * Adaptive authentication settings.
     */
    @NestedConfigurationProperty
    private AdaptiveAuthenticationProperties adaptive = new AdaptiveAuthenticationProperties();

    /**
     * Attribute repository settings.
     */
    @NestedConfigurationProperty
    private PrincipalAttributesProperties attributeRepository = new PrincipalAttributesProperties();

    /**
     * REST-based authentication settings.
     */
    private List<RestAuthenticationProperties> rest = new ArrayList<>(0);

    /**
     * Collection of settings related to LDAP authentication.
     * These settings are required to be indexed (i.e. ldap[0].xyz).
     */
    private List<LdapAuthenticationProperties> ldap = new ArrayList<>(0);

    /**
     * Authentication throttling settings.
     */
    @NestedConfigurationProperty
    private ThrottleProperties throttle = new ThrottleProperties();

    /**
     * SAML identity provider settings.
     */
    @NestedConfigurationProperty
    private SamlIdPProperties samlIdp = new SamlIdPProperties();

    /**
     * Customization of authentication errors and exceptions.
     */
    @NestedConfigurationProperty
    private AuthenticationExceptionsProperties errors = new AuthenticationExceptionsProperties();

    /**
     * Authentication policy settings.
     */
    @NestedConfigurationProperty
    private AuthenticationPolicyProperties policy = new AuthenticationPolicyProperties();

    /**
     * Accepting authentication based on statically defined users.
     */
    @NestedConfigurationProperty
    private AcceptAuthenticationProperties accept = new AcceptAuthenticationProperties();

    /**
     * File-based authentication.
     */
    @NestedConfigurationProperty
    private FileAuthenticationProperties file = new FileAuthenticationProperties();

    /**
     * Blocked authentication.
     */
    @NestedConfigurationProperty
    private RejectAuthenticationProperties reject = new RejectAuthenticationProperties();

    /**
     * Authentication based on a remote of a request.
     */
    @NestedConfigurationProperty
    private RemoteAuthenticationProperties remote = new RemoteAuthenticationProperties();

    /**
     * Authentication settings when integrating CAS with a shibboleth IdP.
     */
    @NestedConfigurationProperty
    private ShibbolethIdPProperties shibIdp = new ShibbolethIdPProperties();

    /**
     * Trusted authentication.
     */
    @NestedConfigurationProperty
    private TrustedAuthenticationProperties trusted = new TrustedAuthenticationProperties();

    /**
     * Collection of settings related to JAAS authentication.
     * These settings are required to be indexed (i.e. jaas[0].xyz).
     */
    private List<JaasAuthenticationProperties> jaas = new ArrayList<>(0);

    /**
     * JDBC authentication settings.
     */
    @NestedConfigurationProperty
    private JdbcAuthenticationProperties jdbc = new JdbcAuthenticationProperties();

    /**
     * MFA settings.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationProperties mfa = new MultifactorAuthenticationProperties();

    /**
     * MongoDb authentication settings.
     */
    @NestedConfigurationProperty
    private MongoDbAuthenticationProperties mongo = new MongoDbAuthenticationProperties();

    /**
     * OAuth authentication settings.
     */
    @NestedConfigurationProperty
    private OAuthProperties oauth = new OAuthProperties();

    /**
     * OpenID Connect authentication settings.
     */
    @NestedConfigurationProperty
    private OidcProperties oidc = new OidcProperties();

    /**
     * Pac4j delegated authentication settings.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationProperties pac4j = new Pac4jDelegatedAuthenticationProperties();

    /**
     * RADIUS authentication settings.
     */
    @NestedConfigurationProperty
    private RadiusProperties radius = new RadiusProperties();

    /**
     * SPNEGO authentication settings.
     */
    @NestedConfigurationProperty
    private SpnegoProperties spnego = new SpnegoProperties();

    /**
     * Collection of settings related to WsFed delegated authentication.
     * These settings are required to be indexed (i.e. wsfed[0].xyz).
     */
    private List<WsFederationDelegationProperties> wsfed = new ArrayList<>(0);

    /**
     * WS-FED IdP authentication settings.
     */
    @NestedConfigurationProperty
    private WsFederationProperties wsfedIdp = new WsFederationProperties();

    /**
     * X509 authentication settings.
     */
    @NestedConfigurationProperty
    private X509Properties x509 = new X509Properties();

    /**
     * Token/JWT authentication settings.
     */
    @NestedConfigurationProperty
    private TokenAuthenticationProperties token = new TokenAuthenticationProperties();

    /**
     * Authentication attribute release settings.
     */
    @NestedConfigurationProperty
    private AuthenticationAttributeReleaseProperties authenticationAttributeRelease = new AuthenticationAttributeReleaseProperties();
}
