package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.configuration.model.support.couchbase.authentication.CouchbaseAuthenticationProperties;
import org.apereo.cas.configuration.model.support.digest.DigestProperties;
import org.apereo.cas.configuration.model.support.fortress.FortressAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RemoteAddressAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.openid.OpenIdProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.rest.RestAuthenticationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.model.support.saml.shibboleth.ShibbolethIdPProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.configuration.model.support.trusted.TrustedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
public class AuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -1233126985007049516L;

    /**
     * Couchbase authentication settings.
     */
    @NestedConfigurationProperty
    private CouchbaseAuthenticationProperties couchbase = new CouchbaseAuthenticationProperties();

    /**
     * Cassandra authentication settings.
     */
    @NestedConfigurationProperty
    private CassandraAuthenticationProperties cassandra = new CassandraAuthenticationProperties();

    /**
     * Cloud Directory authentication settings.
     */
    @NestedConfigurationProperty
    private CloudDirectoryProperties cloudDirectory = new CloudDirectoryProperties();

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
     * Digest authentication settings.
     */
    @NestedConfigurationProperty
    private DigestProperties digest = new DigestProperties();

    /**
     * REST-based authentication settings.
     */
    @NestedConfigurationProperty
    private RestAuthenticationProperties rest = new RestAuthenticationProperties();

    /**
     * Collection of settings related to LDAP authentication.
     * These settings are required to be indexed (i.e. ldap[0].xyz).
     */
    private List<LdapAuthenticationProperties> ldap = new ArrayList<>();

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
    private AuthenticationExceptionsProperties exceptions = new AuthenticationExceptionsProperties();

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
     * Blacklist-based authentication.
     */
    @NestedConfigurationProperty
    private RejectAuthenticationProperties reject = new RejectAuthenticationProperties();

    /**
     * Authentication based on a remote-address of a request.
     */
    @NestedConfigurationProperty
    private RemoteAddressAuthenticationProperties remoteAddress = new RemoteAddressAuthenticationProperties();

    /**
     * Authentication settings when integrating CAS with a shibboleth IdP.
     */
    @NestedConfigurationProperty
    private ShibbolethIdPProperties shibIdp = new ShibbolethIdPProperties();

    /**
     * Shiro-based authentication.
     */
    @NestedConfigurationProperty
    private ShiroAuthenticationProperties shiro = new ShiroAuthenticationProperties();

    /**
     * Trusted authentication.
     */
    @NestedConfigurationProperty
    private TrustedAuthenticationProperties trusted = new TrustedAuthenticationProperties();

    /**
     * Collection of settings related to JAAS authentication.
     * These settings are required to be indexed (i.e. jaas[0].xyz).
     */
    private List<JaasAuthenticationProperties> jaas = new ArrayList<>();

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
    private MongoAuthenticationProperties mongo = new MongoAuthenticationProperties();

    /**
     * NTLM authentication settings.
     */
    @NestedConfigurationProperty
    private NtlmProperties ntlm = new NtlmProperties();

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
     * OpenID authentication settings.
     */
    @NestedConfigurationProperty
    private OpenIdProperties openid = new OpenIdProperties();

    /**
     * Pac4j delegated authentication settings.
     */
    @NestedConfigurationProperty
    private Pac4jProperties pac4j = new Pac4jProperties();

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
    private List<WsFederationDelegationProperties> wsfed = new ArrayList<>();

    /**
     * WS-FED delegated authentication settings.
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
     * Apache Fortress authentication settings.
     */
    @NestedConfigurationProperty
    private FortressAuthenticationProperties fortress = new FortressAuthenticationProperties();

    /**
     * Authentication attribute release settings.
     */
    @NestedConfigurationProperty
    private AuthenticationAttributeReleaseProperties authenticationAttributeRelease = new AuthenticationAttributeReleaseProperties();

    /**
     * Whether CAS authentication/protocol attributes
     * should be released as part of ticket validation.
     */
    private boolean releaseProtocolAttributes = true;

    public ShibbolethIdPProperties getShibIdp() {
        return shibIdp;
    }

    public void setShibIdp(final ShibbolethIdPProperties shibIdp) {
        this.shibIdp = shibIdp;
    }

    public SurrogateAuthenticationProperties getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(final SurrogateAuthenticationProperties surrogate) {
        this.surrogate = surrogate;
    }

    public AuthenticationAttributeReleaseProperties getAuthenticationAttributeRelease() {
        return authenticationAttributeRelease;
    }

    public void setAuthenticationAttributeRelease(final AuthenticationAttributeReleaseProperties authenticationAttributeRelease) {
        this.authenticationAttributeRelease = authenticationAttributeRelease;
    }

    public boolean isReleaseProtocolAttributes() {
        return releaseProtocolAttributes;
    }

    public void setReleaseProtocolAttributes(final boolean releaseProtocolAttributes) {
        this.releaseProtocolAttributes = releaseProtocolAttributes;
    }

    public WsFederationProperties getWsfedIdp() {
        return wsfedIdp;
    }

    public void setWsfedIdp(final WsFederationProperties wsfedIdp) {
        this.wsfedIdp = wsfedIdp;
    }

    public TokenAuthenticationProperties getToken() {
        return token;
    }

    public void setToken(final TokenAuthenticationProperties token) {
        this.token = token;
    }

    public AuthenticationExceptionsProperties getExceptions() {
        return exceptions;
    }

    public void setExceptions(final AuthenticationExceptionsProperties exceptions) {
        this.exceptions = exceptions;
    }

    public AuthenticationPolicyProperties getPolicy() {
        return policy;
    }

    public AcceptAuthenticationProperties getAccept() {
        return accept;
    }

    public void setAccept(final AcceptAuthenticationProperties accept) {
        this.accept = accept;
    }

    public FileAuthenticationProperties getFile() {
        return file;
    }

    public void setFile(final FileAuthenticationProperties file) {
        this.file = file;
    }

    public RejectAuthenticationProperties getReject() {
        return reject;
    }

    public void setReject(final RejectAuthenticationProperties reject) {
        this.reject = reject;
    }

    public RemoteAddressAuthenticationProperties getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final RemoteAddressAuthenticationProperties remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public ShiroAuthenticationProperties getShiro() {
        return shiro;
    }

    public void setShiro(final ShiroAuthenticationProperties shiro) {
        this.shiro = shiro;
    }

    public List<JaasAuthenticationProperties> getJaas() {
        return jaas;
    }

    public void setJaas(final List<JaasAuthenticationProperties> jaas) {
        this.jaas = jaas;
    }

    public JdbcAuthenticationProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(final JdbcAuthenticationProperties jdbc) {
        this.jdbc = jdbc;
    }

    public MultifactorAuthenticationProperties getMfa() {
        return mfa;
    }

    public void setMfa(final MultifactorAuthenticationProperties mfa) {
        this.mfa = mfa;
    }

    public MongoAuthenticationProperties getMongo() {
        return mongo;
    }

    public void setMongo(final MongoAuthenticationProperties mongo) {
        this.mongo = mongo;
    }

    public NtlmProperties getNtlm() {
        return ntlm;
    }

    public void setNtlm(final NtlmProperties ntlm) {
        this.ntlm = ntlm;
    }

    public OAuthProperties getOauth() {
        return oauth;
    }

    public void setOauth(final OAuthProperties oauth) {
        this.oauth = oauth;
    }

    public OidcProperties getOidc() {
        return oidc;
    }

    public void setOidc(final OidcProperties oidc) {
        this.oidc = oidc;
    }

    public OpenIdProperties getOpenid() {
        return openid;
    }

    public void setOpenid(final OpenIdProperties openid) {
        this.openid = openid;
    }

    public Pac4jProperties getPac4j() {
        return pac4j;
    }

    public void setPac4j(final Pac4jProperties pac4j) {
        this.pac4j = pac4j;
    }

    public RadiusProperties getRadius() {
        return radius;
    }

    public void setRadius(final RadiusProperties radius) {
        this.radius = radius;
    }

    public SpnegoProperties getSpnego() {
        return spnego;
    }

    public void setSpnego(final SpnegoProperties spnego) {
        this.spnego = spnego;
    }
    
    public List<WsFederationDelegationProperties> getWsfed() {
        return wsfed;
    }

    public void setWsfed(final List<WsFederationDelegationProperties> wsfed) {
        this.wsfed = wsfed;
    }

    public X509Properties getX509() {
        return x509;
    }

    public void setX509(final X509Properties x509) {
        this.x509 = x509;
    }

    public SamlIdPProperties getSamlIdp() {
        return samlIdp;
    }

    public void setSamlIdp(final SamlIdPProperties samlIdp) {
        this.samlIdp = samlIdp;
    }

    public ThrottleProperties getThrottle() {
        return throttle;
    }

    public void setThrottle(final ThrottleProperties throttle) {
        this.throttle = throttle;
    }

    public TrustedAuthenticationProperties getTrusted() {
        return trusted;
    }

    public void setTrusted(final TrustedAuthenticationProperties trusted) {
        this.trusted = trusted;
    }
    
    public List<LdapAuthenticationProperties> getLdap() {
        return ldap;
    }

    public void setLdap(final List<LdapAuthenticationProperties> ldap) {
        this.ldap = ldap;
    }

    public RestAuthenticationProperties getRest() {
        return rest;
    }

    public void setRest(final RestAuthenticationProperties rest) {
        this.rest = rest;
    }

    public DigestProperties getDigest() {
        return digest;
    }

    public void setDigest(final DigestProperties digest) {
        this.digest = digest;
    }

    public PrincipalAttributesProperties getAttributeRepository() {
        return attributeRepository;
    }

    public void setAttributeRepository(final PrincipalAttributesProperties attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public AdaptiveAuthenticationProperties getAdaptive() {
        return adaptive;
    }

    public void setAdaptive(final AdaptiveAuthenticationProperties adaptive) {
        this.adaptive = adaptive;
    }

    public void setPolicy(final AuthenticationPolicyProperties policy) {
        this.policy = policy;
    }

    public PasswordManagementProperties getPm() {
        return pm;
    }

    public void setPm(final PasswordManagementProperties pm) {
        this.pm = pm;
    }

    public GraphicalUserAuthenticationProperties getGua() {
        return gua;
    }

    public void setGua(final GraphicalUserAuthenticationProperties gua) {
        this.gua = gua;
    }

    public CloudDirectoryProperties getCloudDirectory() {
        return cloudDirectory;
    }

    public void setCloudDirectory(final CloudDirectoryProperties cloudDirectory) {
        this.cloudDirectory = cloudDirectory;
    }

    public CassandraAuthenticationProperties getCassandra() {
        return cassandra;
    }

    public void setCassandra(final CassandraAuthenticationProperties cassandra) {
        this.cassandra = cassandra;
    }

    public CouchbaseAuthenticationProperties getCouchbase() {
        return couchbase;
    }

    public void setCouchbase(final CouchbaseAuthenticationProperties couchbase) {
        this.couchbase = couchbase;
    }

    public FortressAuthenticationProperties getFortress() {
        return fortress;
    }

    public void setFortress(final FortressAuthenticationProperties fortress) {
        this.fortress = fortress;
    }
}
