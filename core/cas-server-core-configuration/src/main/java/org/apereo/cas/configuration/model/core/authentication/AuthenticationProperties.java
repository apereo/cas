package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.digest.DigestProperties;
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
import org.apereo.cas.configuration.model.support.stormpath.StormpathProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.configuration.model.support.trusted.TrustedAuthenticationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthenticationProperties {

    @NestedConfigurationProperty
    private SurrogateAuthenticationProperties surrogate = new SurrogateAuthenticationProperties();
    
    @NestedConfigurationProperty
    private GraphicalUserAuthenticationProperties gua = new GraphicalUserAuthenticationProperties();
    
    @NestedConfigurationProperty
    private PasswordManagementProperties pm = new PasswordManagementProperties();

    @NestedConfigurationProperty
    private AdaptiveAuthenticationProperties adaptive = new AdaptiveAuthenticationProperties();

    @NestedConfigurationProperty
    private PrincipalAttributesProperties attributeRepository = new PrincipalAttributesProperties();

    @NestedConfigurationProperty
    private DigestProperties digest = new DigestProperties();

    @NestedConfigurationProperty
    private RestAuthenticationProperties rest = new RestAuthenticationProperties();

    @NestedConfigurationProperty
    private List<LdapAuthenticationProperties> ldap = new ArrayList<>();

    @NestedConfigurationProperty
    private ThrottleProperties throttle = new ThrottleProperties();

    @NestedConfigurationProperty
    private SamlIdPProperties samlIdp = new SamlIdPProperties();

    @NestedConfigurationProperty
    private AuthenticationExceptionsProperties exceptions = new AuthenticationExceptionsProperties();

    @NestedConfigurationProperty
    private AuthenticationPolicyProperties policy = new AuthenticationPolicyProperties();

    @NestedConfigurationProperty
    private AcceptAuthenticationProperties accept = new AcceptAuthenticationProperties();

    @NestedConfigurationProperty
    private FileAuthenticationProperties file = new FileAuthenticationProperties();

    @NestedConfigurationProperty
    private RejectAuthenticationProperties reject = new RejectAuthenticationProperties();

    @NestedConfigurationProperty
    private RemoteAddressAuthenticationProperties remoteAddress = new RemoteAddressAuthenticationProperties();

    @NestedConfigurationProperty
    private ShibbolethIdPProperties shibIdP = new ShibbolethIdPProperties();
    
    @NestedConfigurationProperty
    private ShiroAuthenticationProperties shiro = new ShiroAuthenticationProperties();

    @NestedConfigurationProperty
    private TrustedAuthenticationProperties trusted = new TrustedAuthenticationProperties();

    @NestedConfigurationProperty
    private List<JaasAuthenticationProperties> jaas = new ArrayList<>();

    @NestedConfigurationProperty
    private JdbcAuthenticationProperties jdbc = new JdbcAuthenticationProperties();

    @NestedConfigurationProperty
    private MultifactorAuthenticationProperties mfa = new MultifactorAuthenticationProperties();

    @NestedConfigurationProperty
    private MongoAuthenticationProperties mongo = new MongoAuthenticationProperties();

    @NestedConfigurationProperty
    private NtlmProperties ntlm = new NtlmProperties();

    @NestedConfigurationProperty
    private OAuthProperties oauth = new OAuthProperties();

    @NestedConfigurationProperty
    private OidcProperties oidc = new OidcProperties();

    @NestedConfigurationProperty
    private OpenIdProperties openid = new OpenIdProperties();

    @NestedConfigurationProperty
    private Pac4jProperties pac4j = new Pac4jProperties();

    @NestedConfigurationProperty
    private RadiusProperties radius = new RadiusProperties();

    @NestedConfigurationProperty
    private SpnegoProperties spnego = new SpnegoProperties();

    @NestedConfigurationProperty
    private StormpathProperties stormpath = new StormpathProperties();

    @NestedConfigurationProperty
    private WsFederationDelegationProperties wsfed = new WsFederationDelegationProperties();

    @NestedConfigurationProperty
    private WsFederationProperties wsfedIdP = new WsFederationProperties();
    
    @NestedConfigurationProperty
    private X509Properties x509 = new X509Properties();

    @NestedConfigurationProperty
    private TokenAuthenticationProperties token = new TokenAuthenticationProperties();

    private boolean releaseProtocolAttributes = true;

    public ShibbolethIdPProperties getShibIdP() {
        return shibIdP;
    }

    public void setShibIdP(final ShibbolethIdPProperties shibIdP) {
        this.shibIdP = shibIdP;
    }

    public SurrogateAuthenticationProperties getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(final SurrogateAuthenticationProperties surrogate) {
        this.surrogate = surrogate;
    }

    public boolean isReleaseProtocolAttributes() {
        return releaseProtocolAttributes;
    }

    public void setReleaseProtocolAttributes(final boolean releaseProtocolAttributes) {
        this.releaseProtocolAttributes = releaseProtocolAttributes;
    }

    public WsFederationProperties getWsfedIdP() {
        return wsfedIdP;
    }

    public void setWsfedIdP(final WsFederationProperties wsfedIdP) {
        this.wsfedIdP = wsfedIdP;
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

    public StormpathProperties getStormpath() {
        return stormpath;
    }

    public void setStormpath(final StormpathProperties stormpath) {
        this.stormpath = stormpath;
    }

    public WsFederationDelegationProperties getWsfed() {
        return wsfed;
    }

    public void setWsfed(final WsFederationDelegationProperties wsfed) {
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
}
