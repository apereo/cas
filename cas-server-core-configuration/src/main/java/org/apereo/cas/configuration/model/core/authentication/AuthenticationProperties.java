package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RemoteAddressAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.MfaProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.openid.OpenIdProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.model.support.stormpath.StormpathProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link AuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthenticationProperties {

    @NestedConfigurationProperty
    private SamlIdPProperties samlIdp = new SamlIdPProperties();
    
    @NestedConfigurationProperty
    private AuthenticationExceptionsProperties exceptions =
            new AuthenticationExceptionsProperties();

    @NestedConfigurationProperty
    private AuthenticationPolicyProperties policy =
            new AuthenticationPolicyProperties();

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

    @NestedConfigurationProperty
    private AcceptAuthenticationProperties accept = new AcceptAuthenticationProperties();

    @NestedConfigurationProperty
    private FileAuthenticationProperties file = new FileAuthenticationProperties();

    @NestedConfigurationProperty
    private RejectAuthenticationProperties reject = new RejectAuthenticationProperties();

    @NestedConfigurationProperty
    private RemoteAddressAuthenticationProperties remoteAddress = new RemoteAddressAuthenticationProperties();

    @NestedConfigurationProperty
    private ShiroAuthenticationProperties shiro = new ShiroAuthenticationProperties();

    @NestedConfigurationProperty
    private JaasAuthenticationProperties jaas = new JaasAuthenticationProperties();

    @NestedConfigurationProperty
    private JdbcAuthenticationProperties jdbc = new JdbcAuthenticationProperties();

    @NestedConfigurationProperty
    private MfaProperties mfa = new MfaProperties();
    
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
    private WsFederationProperties wsfed = new WsFederationProperties();

    @NestedConfigurationProperty
    private X509Properties x509 = new X509Properties();
    
    public AuthenticationExceptionsProperties getExceptions() {
        return exceptions;
    }

    public void setExceptions(final AuthenticationExceptionsProperties exceptions) {
        this.exceptions = exceptions;
    }

    public AuthenticationPolicyProperties getPolicy() {
        return policy;
    }

    public void setPolicy(final AuthenticationPolicyProperties policy) {
        this.policy = policy;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordPolicyProperties getPasswordPolicy() {
        return passwordPolicy;
    }

    public void setPasswordPolicy(final PasswordPolicyProperties passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
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

    public JaasAuthenticationProperties getJaas() {
        return jaas;
    }

    public void setJaas(final JaasAuthenticationProperties jaas) {
        this.jaas = jaas;
    }

    public JdbcAuthenticationProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(final JdbcAuthenticationProperties jdbc) {
        this.jdbc = jdbc;
    }

    public MfaProperties getMfa() {
        return mfa;
    }

    public void setMfa(final MfaProperties mfa) {
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

    public WsFederationProperties getWsfed() {
        return wsfed;
    }

    public void setWsfed(final WsFederationProperties wsfed) {
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
}
