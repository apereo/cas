package org.apereo.cas.configuration;

import org.apereo.cas.configuration.model.core.HostProperties;
import org.apereo.cas.configuration.model.core.ServerProperties;
import org.apereo.cas.configuration.model.core.audit.AuditProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationExceptionsProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.events.EventsProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.configuration.model.core.metrics.MetricsProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.configuration.model.core.rest.RegisteredServiceRestProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.configuration.model.core.slo.SloProperties;
import org.apereo.cas.configuration.model.core.sso.SsoProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ProxyTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.ServiceTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.model.core.util.TicketProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.model.core.web.security.HttpWebRequestProperties;
import org.apereo.cas.configuration.model.core.web.view.ViewProperties;
import org.apereo.cas.configuration.model.support.analytics.GoogleAnalyticsProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.clearpass.ClearpassProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RemoteAddressAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedProperties;
import org.apereo.cas.configuration.model.support.mfa.MfaProperties;
import org.apereo.cas.configuration.model.support.mongo.MongoAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.ntlm.NtlmProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.openid.OpenIdProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.saml.SamlResponseProperties;
import org.apereo.cas.configuration.model.support.saml.googleapps.GoogleAppsProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.model.support.saml.mdui.SamlMetadataUIProperties;
import org.apereo.cas.configuration.model.support.saml.shibboleth.AttributeResolverProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.model.support.stormpath.StormpathProperties;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.configuration.model.support.x509.X509Properties;
import org.apereo.cas.configuration.model.webapp.LocaleProperties;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.configuration.model.webapp.mgmt.ManagementWebappProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CasConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas")
public class CasConfigurationProperties {
    
    @NestedConfigurationProperty
    private AuditProperties audit = new AuditProperties();

    @NestedConfigurationProperty
    private AuthenticationExceptionsProperties authnExceptions = 
            new AuthenticationExceptionsProperties();

    @NestedConfigurationProperty
    private AuthenticationPolicyProperties authnPolicy = 
            new AuthenticationPolicyProperties();

    @NestedConfigurationProperty
    private HttpClientProperties httpClient = new HttpClientProperties();

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties personDirectory = 
            new PersonDirPrincipalResolverProperties();

    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = 
            new PrincipalTransformationProperties();

    @NestedConfigurationProperty
    private EventsProperties events = new EventsProperties();

    @NestedConfigurationProperty
    private HostProperties host = new HostProperties();

    @NestedConfigurationProperty
    private LogoutProperties logout = new LogoutProperties();

    @NestedConfigurationProperty
    private MetricsProperties metrics = new MetricsProperties();

    @NestedConfigurationProperty
    private MonitorProperties monitor = new MonitorProperties();

    @NestedConfigurationProperty
    private RegisteredServiceRestProperties restServices = new RegisteredServiceRestProperties();

    @NestedConfigurationProperty
    private ServerProperties server = new ServerProperties();

    @NestedConfigurationProperty
    private ServiceRegistryProperties serviceRegistry = new ServiceRegistryProperties();

    @NestedConfigurationProperty
    private SloProperties slo = new SloProperties();

    @NestedConfigurationProperty
    private SsoProperties sso = new SsoProperties();

    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties pgt = new ProxyGrantingTicketProperties();

    @NestedConfigurationProperty
    private ProxyTicketProperties pt = new ProxyTicketProperties();

    @NestedConfigurationProperty
    private TicketRegistryProperties ticketRegistry = new TicketRegistryProperties();

    @NestedConfigurationProperty
    private ServiceTicketProperties st = new ServiceTicketProperties();

    @NestedConfigurationProperty
    private TicketGrantingTicketProperties tgt = new TicketGrantingTicketProperties();
    
    @NestedConfigurationProperty
    private TicketProperties ticket = new TicketProperties();

    @NestedConfigurationProperty
    private MessageBundleProperties messageBundle = new MessageBundleProperties();

    @NestedConfigurationProperty
    private AdminPagesSecurityProperties adminPagesSecurity = new AdminPagesSecurityProperties();

    @NestedConfigurationProperty
    private HttpWebRequestProperties httpWebRequest = new HttpWebRequestProperties();

    @NestedConfigurationProperty
    private ViewProperties view = new ViewProperties();

    @NestedConfigurationProperty
    private GoogleAnalyticsProperties googleAnalytics = new GoogleAnalyticsProperties();

    @NestedConfigurationProperty
    private AcceptableUsagePolicyProperties acceptableUsagePolicy = new AcceptableUsagePolicyProperties();

    @NestedConfigurationProperty
    private ClearpassProperties clearpass = new ClearpassProperties();
    
    @NestedConfigurationProperty
    private TicketGrantingCookieProperties tgc = new TicketGrantingCookieProperties();

    @NestedConfigurationProperty
    private WarningCookieProperties warningCookie = new WarningCookieProperties();

    @NestedConfigurationProperty
    private CouchbaseServiceRegistryProperties couchbaseServiceRegistry = new CouchbaseServiceRegistryProperties();

    @NestedConfigurationProperty
    private CouchbaseTicketRegistryProperties couchbaseTicketRegistry = new CouchbaseTicketRegistryProperties();

    @NestedConfigurationProperty
    private EhcacheProperties ehcache = new EhcacheProperties();

    @NestedConfigurationProperty
    private AcceptAuthenticationProperties acceptAuthn = new AcceptAuthenticationProperties();

    @NestedConfigurationProperty
    private FileAuthenticationProperties fileAuthn = new FileAuthenticationProperties();

    @NestedConfigurationProperty
    private RejectAuthenticationProperties rejectAuthn = new RejectAuthenticationProperties();

    @NestedConfigurationProperty
    private RemoteAddressAuthenticationProperties remoteAddressAuthn = new RemoteAddressAuthenticationProperties();

    @NestedConfigurationProperty
    private ShiroAuthenticationProperties shiroAuthn = new ShiroAuthenticationProperties();

    @NestedConfigurationProperty
    private MaxmindProperties maxmind = new MaxmindProperties();

    @NestedConfigurationProperty
    private HazelcastProperties hazelcast = new HazelcastProperties();

    @NestedConfigurationProperty
    private IgniteProperties ignite = new IgniteProperties();

    @NestedConfigurationProperty
    private JaasAuthenticationProperties jaas = new JaasAuthenticationProperties();

    @NestedConfigurationProperty
    private JdbcAuthenticationProperties jdbcAuthn = new JdbcAuthenticationProperties();
    
    @NestedConfigurationProperty
    private DatabaseProperties jdbc = new DatabaseProperties();
    
    @NestedConfigurationProperty
    private JpaServiceRegistryProperties jpaServiceRegistry = new JpaServiceRegistryProperties();

    @NestedConfigurationProperty
    private JpaTicketRegistryProperties jpaTicketRegistry = new JpaTicketRegistryProperties();

    @NestedConfigurationProperty
    private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();

    @NestedConfigurationProperty
    private LdapServiceRegistryProperties ldapServiceRegistry = new LdapServiceRegistryProperties();

    @NestedConfigurationProperty
    private MemcachedProperties memcached = new MemcachedProperties();

    @NestedConfigurationProperty
    private MfaProperties mfa = new MfaProperties();

    @NestedConfigurationProperty
    private MongoAuthenticationProperties mongoAuthn = new MongoAuthenticationProperties();

    @NestedConfigurationProperty
    private MongoServiceRegistryProperties mongoServiceRegistry = new MongoServiceRegistryProperties();

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
    private GoogleAppsProperties googleApps = new GoogleAppsProperties();

    @NestedConfigurationProperty
    private SamlIdPProperties samlIdp = new SamlIdPProperties();

    @NestedConfigurationProperty
    private SamlMetadataUIProperties samlMetadataUi = new SamlMetadataUIProperties();

    @NestedConfigurationProperty
    private SamlResponseProperties samlResponse = new SamlResponseProperties();

    @NestedConfigurationProperty
    private AttributeResolverProperties shibAttributeResolver = new AttributeResolverProperties();

    @NestedConfigurationProperty
    private SpnegoProperties spnego = new SpnegoProperties();

    @NestedConfigurationProperty
    private StormpathProperties stormpath = new StormpathProperties();

    @NestedConfigurationProperty
    private ThemeProperties theme = new ThemeProperties();

    @NestedConfigurationProperty
    private ThrottleProperties throttle = new ThrottleProperties();

    @NestedConfigurationProperty
    private WsFederationProperties wsfed = new WsFederationProperties();

    @NestedConfigurationProperty
    private X509Properties x509 = new X509Properties();

    @NestedConfigurationProperty
    private LocaleProperties locale = new LocaleProperties();

    @NestedConfigurationProperty
    private ManagementWebappProperties mgmt = new ManagementWebappProperties();

    @NestedConfigurationProperty
    private WebflowProperties webflow = new WebflowProperties();
    
    public AuditProperties getAudit() {
        return audit;
    }

    public void setAudit(final AuditProperties audit) {
        this.audit = audit;
    }

    public AuthenticationExceptionsProperties getAuthnExceptions() {
        return authnExceptions;
    }

    public void setAuthnExceptions(final AuthenticationExceptionsProperties authnExceptions) {
        this.authnExceptions = authnExceptions;
    }

    public AuthenticationPolicyProperties getAuthnPolicy() {
        return authnPolicy;
    }

    public void setAuthnPolicy(final AuthenticationPolicyProperties authnPolicy) {
        this.authnPolicy = authnPolicy;
    }

    public HttpClientProperties getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(final HttpClientProperties httpClient) {
        this.httpClient = httpClient;
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

    public PersonDirPrincipalResolverProperties getPersonDirectory() {
        return personDirectory;
    }

    public void setPersonDirectory(final PersonDirPrincipalResolverProperties personDirectory) {
        this.personDirectory = personDirectory;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public EventsProperties getEvents() {
        return events;
    }

    public void setEvents(final EventsProperties events) {
        this.events = events;
    }

    public HostProperties getHost() {
        return host;
    }

    public void setHost(final HostProperties host) {
        this.host = host;
    }

    public LogoutProperties getLogout() {
        return logout;
    }

    public void setLogout(final LogoutProperties logout) {
        this.logout = logout;
    }

    public MetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(final MetricsProperties metrics) {
        this.metrics = metrics;
    }

    public MonitorProperties getMonitor() {
        return monitor;
    }

    public void setMonitor(final MonitorProperties monitor) {
        this.monitor = monitor;
    }

    public RegisteredServiceRestProperties getRestServices() {
        return restServices;
    }

    public void setRestServices(final RegisteredServiceRestProperties restServices) {
        this.restServices = restServices;
    }

    public ServerProperties getServer() {
        return server;
    }

    public void setServer(final ServerProperties server) {
        this.server = server;
    }

    public ServiceRegistryProperties getServiceRegistry() {
        return serviceRegistry;
    }

    public void setServiceRegistry(final ServiceRegistryProperties serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public SloProperties getSlo() {
        return slo;
    }

    public void setSlo(final SloProperties slo) {
        this.slo = slo;
    }

    public SsoProperties getSso() {
        return sso;
    }

    public void setSso(final SsoProperties sso) {
        this.sso = sso;
    }

    public ProxyGrantingTicketProperties getPgt() {
        return pgt;
    }

    public void setPgt(final ProxyGrantingTicketProperties pgt) {
        this.pgt = pgt;
    }

    public ProxyTicketProperties getPt() {
        return pt;
    }

    public void setPt(final ProxyTicketProperties pt) {
        this.pt = pt;
    }

    public TicketRegistryProperties getTicketRegistry() {
        return ticketRegistry;
    }

    public void setTicketRegistry(final TicketRegistryProperties ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public ServiceTicketProperties getSt() {
        return st;
    }

    public void setSt(final ServiceTicketProperties st) {
        this.st = st;
    }

    public TicketGrantingTicketProperties getTgt() {
        return tgt;
    }

    public void setTgt(final TicketGrantingTicketProperties tgt) {
        this.tgt = tgt;
    }
    

    public TicketProperties getTicket() {
        return ticket;
    }

    public void setTicket(final TicketProperties ticket) {
        this.ticket = ticket;
    }

    public MessageBundleProperties getMessageBundle() {
        return messageBundle;
    }

    public void setMessageBundle(final MessageBundleProperties messageBundle) {
        this.messageBundle = messageBundle;
    }

    public AdminPagesSecurityProperties getAdminPagesSecurity() {
        return adminPagesSecurity;
    }

    public void setAdminPagesSecurity(final AdminPagesSecurityProperties adminPagesSecurity) {
        this.adminPagesSecurity = adminPagesSecurity;
    }

    public HttpWebRequestProperties getHttpWebRequest() {
        return httpWebRequest;
    }

    public void setHttpWebRequest(final HttpWebRequestProperties httpWebRequest) {
        this.httpWebRequest = httpWebRequest;
    }

    public ViewProperties getView() {
        return view;
    }

    public void setView(final ViewProperties view) {
        this.view = view;
    }

    public GoogleAnalyticsProperties getGoogleAnalytics() {
        return googleAnalytics;
    }

    public void setGoogleAnalytics(final GoogleAnalyticsProperties googleAnalytics) {
        this.googleAnalytics = googleAnalytics;
    }

    public AcceptableUsagePolicyProperties getAcceptableUsagePolicy() {
        return acceptableUsagePolicy;
    }

    public void setAcceptableUsagePolicy(final AcceptableUsagePolicyProperties acceptableUsagePolicy) {
        this.acceptableUsagePolicy = acceptableUsagePolicy;
    }

    public ClearpassProperties getClearpass() {
        return clearpass;
    }

    public void setClearpass(final ClearpassProperties clearpass) {
        this.clearpass = clearpass;
    }
    

    public TicketGrantingCookieProperties getTgc() {
        return tgc;
    }

    public void setTgc(final TicketGrantingCookieProperties tgc) {
        this.tgc = tgc;
    }

    public WarningCookieProperties getWarningCookie() {
        return warningCookie;
    }

    public void setWarningCookie(final WarningCookieProperties warningCookie) {
        this.warningCookie = warningCookie;
    }

    public CouchbaseServiceRegistryProperties getCouchbaseServiceRegistry() {
        return couchbaseServiceRegistry;
    }

    public void setCouchbaseServiceRegistry(final CouchbaseServiceRegistryProperties couchbaseServiceRegistry) {
        this.couchbaseServiceRegistry = couchbaseServiceRegistry;
    }

    public CouchbaseTicketRegistryProperties getCouchbaseTicketRegistry() {
        return couchbaseTicketRegistry;
    }

    public void setCouchbaseTicketRegistry(final CouchbaseTicketRegistryProperties couchbaseTicketRegistry) {
        this.couchbaseTicketRegistry = couchbaseTicketRegistry;
    }

    public EhcacheProperties getEhcache() {
        return ehcache;
    }

    public void setEhcache(final EhcacheProperties ehcache) {
        this.ehcache = ehcache;
    }

    public AcceptAuthenticationProperties getAcceptAuthn() {
        return acceptAuthn;
    }

    public void setAcceptAuthn(final AcceptAuthenticationProperties acceptAuthn) {
        this.acceptAuthn = acceptAuthn;
    }

    public FileAuthenticationProperties getFileAuthn() {
        return fileAuthn;
    }

    public void setFileAuthn(final FileAuthenticationProperties fileAuthn) {
        this.fileAuthn = fileAuthn;
    }

    public RejectAuthenticationProperties getRejectAuthn() {
        return rejectAuthn;
    }

    public void setRejectAuthn(final RejectAuthenticationProperties rejectAuthn) {
        this.rejectAuthn = rejectAuthn;
    }

    public RemoteAddressAuthenticationProperties getRemoteAddressAuthn() {
        return remoteAddressAuthn;
    }

    public void setRemoteAddressAuthn(final RemoteAddressAuthenticationProperties remoteAddressAuthn) {
        this.remoteAddressAuthn = remoteAddressAuthn;
    }

    public ShiroAuthenticationProperties getShiroAuthn() {
        return shiroAuthn;
    }

    public void setShiroAuthn(final ShiroAuthenticationProperties shiroAuthn) {
        this.shiroAuthn = shiroAuthn;
    }

    public MaxmindProperties getMaxmind() {
        return maxmind;
    }

    public void setMaxmind(final MaxmindProperties maxmind) {
        this.maxmind = maxmind;
    }

    public HazelcastProperties getHazelcast() {
        return hazelcast;
    }

    public void setHazelcast(final HazelcastProperties hazelcast) {
        this.hazelcast = hazelcast;
    }

    public IgniteProperties getIgnite() {
        return ignite;
    }

    public void setIgnite(final IgniteProperties ignite) {
        this.ignite = ignite;
    }

    public JaasAuthenticationProperties getJaas() {
        return jaas;
    }

    public void setJaas(final JaasAuthenticationProperties jaas) {
        this.jaas = jaas;
    }

    public JdbcAuthenticationProperties getJdbcAuthn() {
        return jdbcAuthn;
    }

    public void setJdbcAuthn(final JdbcAuthenticationProperties jdbcAuthn) {
        this.jdbcAuthn = jdbcAuthn;
    }
    

    public DatabaseProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(final DatabaseProperties jdbc) {
        this.jdbc = jdbc;
    }

    public JpaServiceRegistryProperties getJpaServiceRegistry() {
        return jpaServiceRegistry;
    }

    public void setJpaServiceRegistry(final JpaServiceRegistryProperties jpaServiceRegistry) {
        this.jpaServiceRegistry = jpaServiceRegistry;
    }

    public JpaTicketRegistryProperties getJpaTicketRegistry() {
        return jpaTicketRegistry;
    }

    public void setJpaTicketRegistry(final JpaTicketRegistryProperties jpaTicketRegistry) {
        this.jpaTicketRegistry = jpaTicketRegistry;
    }

    public LdapAuthorizationProperties getLdapAuthz() {
        return ldapAuthz;
    }

    public void setLdapAuthz(final LdapAuthorizationProperties ldapAuthz) {
        this.ldapAuthz = ldapAuthz;
    }

    public LdapServiceRegistryProperties getLdapServiceRegistry() {
        return ldapServiceRegistry;
    }

    public void setLdapServiceRegistry(final LdapServiceRegistryProperties ldapServiceRegistry) {
        this.ldapServiceRegistry = ldapServiceRegistry;
    }

    public MemcachedProperties getMemcached() {
        return memcached;
    }

    public void setMemcached(final MemcachedProperties memcached) {
        this.memcached = memcached;
    }

    public MfaProperties getMfa() {
        return mfa;
    }

    public void setMfa(final MfaProperties mfa) {
        this.mfa = mfa;
    }

    public MongoAuthenticationProperties getMongoAuthn() {
        return mongoAuthn;
    }

    public void setMongoAuthn(final MongoAuthenticationProperties mongoAuthn) {
        this.mongoAuthn = mongoAuthn;
    }

    public MongoServiceRegistryProperties getMongoServiceRegistry() {
        return mongoServiceRegistry;
    }

    public void setMongoServiceRegistry(final MongoServiceRegistryProperties mongoServiceRegistry) {
        this.mongoServiceRegistry = mongoServiceRegistry;
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

    public GoogleAppsProperties getGoogleApps() {
        return googleApps;
    }

    public void setGoogleApps(final GoogleAppsProperties googleApps) {
        this.googleApps = googleApps;
    }

    public SamlIdPProperties getSamlIdp() {
        return samlIdp;
    }

    public void setSamlIdp(final SamlIdPProperties samlIdp) {
        this.samlIdp = samlIdp;
    }

    public SamlMetadataUIProperties getSamlMetadataUi() {
        return samlMetadataUi;
    }

    public void setSamlMetadataUi(final SamlMetadataUIProperties samlMetadataUi) {
        this.samlMetadataUi = samlMetadataUi;
    }

    public SamlResponseProperties getSamlResponse() {
        return samlResponse;
    }

    public void setSamlResponse(final SamlResponseProperties samlResponse) {
        this.samlResponse = samlResponse;
    }

    public AttributeResolverProperties getShibAttributeResolver() {
        return shibAttributeResolver;
    }

    public void setShibAttributeResolver(final AttributeResolverProperties shibAttributeResolver) {
        this.shibAttributeResolver = shibAttributeResolver;
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

    public ThemeProperties getTheme() {
        return theme;
    }

    public void setTheme(final ThemeProperties theme) {
        this.theme = theme;
    }

    public ThrottleProperties getThrottle() {
        return throttle;
    }

    public void setThrottle(final ThrottleProperties throttle) {
        this.throttle = throttle;
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

    public LocaleProperties getLocale() {
        return locale;
    }

    public void setLocale(final LocaleProperties locale) {
        this.locale = locale;
    }

    public ManagementWebappProperties getMgmt() {
        return mgmt;
    }

    public void setMgmt(final ManagementWebappProperties mgmt) {
        this.mgmt = mgmt;
    }

    public WebflowProperties getWebflow() {
        return webflow;
    }

    public void setWebflow(final WebflowProperties webflow) {
        this.webflow = webflow;
    }
}
