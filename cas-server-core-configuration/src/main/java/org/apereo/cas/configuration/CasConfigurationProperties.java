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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CasConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas")
@EnableConfigurationProperties(AcceptAuthenticationProperties.class)
public class CasConfigurationProperties {
    
    @NestedConfigurationProperty
    private AuditProperties auditProperties = new AuditProperties();

    @NestedConfigurationProperty
    private AuthenticationExceptionsProperties authenticationExceptionsProperties = new AuthenticationExceptionsProperties();

    @NestedConfigurationProperty
    private AuthenticationPolicyProperties authenticationPolicyProperties = new AuthenticationPolicyProperties();

    @NestedConfigurationProperty
    private HttpClientProperties httpClientProperties = new HttpClientProperties();

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoderProperties = new PasswordEncoderProperties();

    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicyProperties = new PasswordPolicyProperties();

    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties personDirPrincipalResolverProperties = new PersonDirPrincipalResolverProperties();

    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformationProperties = new PrincipalTransformationProperties();

    @NestedConfigurationProperty
    private EventsProperties eventsProperties = new EventsProperties();

    @NestedConfigurationProperty
    private HostProperties hostProperties = new HostProperties();

    @NestedConfigurationProperty
    private LogoutProperties logoutProperties = new LogoutProperties();

    @NestedConfigurationProperty
    private MetricsProperties metricsProperties = new MetricsProperties();

    @NestedConfigurationProperty
    private MonitorProperties monitorProperties = new MonitorProperties();

    @NestedConfigurationProperty
    private RegisteredServiceRestProperties registeredServiceRestProperties = new RegisteredServiceRestProperties();

    @NestedConfigurationProperty
    private ServerProperties serverProperties = new ServerProperties();

    @NestedConfigurationProperty
    private ServiceRegistryProperties serviceRegistryProperties = new ServiceRegistryProperties();

    @NestedConfigurationProperty
    private SloProperties sloProperties = new SloProperties();

    @NestedConfigurationProperty
    private SsoProperties ssoProperties = new SsoProperties();

    @NestedConfigurationProperty
    private ProxyGrantingTicketProperties proxyGrantingTicketProperties = new ProxyGrantingTicketProperties();

    @NestedConfigurationProperty
    private ProxyTicketProperties proxyTicketProperties = new ProxyTicketProperties();

    @NestedConfigurationProperty
    private TicketRegistryProperties ticketRegistryProperties = new TicketRegistryProperties();

    @NestedConfigurationProperty
    private ServiceTicketProperties serviceTicketProperties = new ServiceTicketProperties();

    @NestedConfigurationProperty
    private TicketGrantingTicketProperties ticketGrantingTicketProperties = new TicketGrantingTicketProperties();
    
    @NestedConfigurationProperty
    private TicketProperties ticketProperties = new TicketProperties();

    @NestedConfigurationProperty
    private MessageBundleProperties messageBundleProperties = new MessageBundleProperties();

    @NestedConfigurationProperty
    private AdminPagesSecurityProperties adminPagesSecurityProperties = new AdminPagesSecurityProperties();

    @NestedConfigurationProperty
    private HttpWebRequestProperties httpWebRequestProperties = new HttpWebRequestProperties();

    @NestedConfigurationProperty
    private ViewProperties viewProperties = new ViewProperties();

    @NestedConfigurationProperty
    private GoogleAnalyticsProperties googleAnalyticsProperties = new GoogleAnalyticsProperties();

    @NestedConfigurationProperty
    private AcceptableUsagePolicyProperties acceptableUsagePolicyProperties = new AcceptableUsagePolicyProperties();

    @NestedConfigurationProperty
    private ClearpassProperties clearpassProperties = new ClearpassProperties();
    
    @NestedConfigurationProperty
    private TicketGrantingCookieProperties ticketGrantingCookieProperties = new TicketGrantingCookieProperties();

    @NestedConfigurationProperty
    private WarningCookieProperties warningCookieProperties = new WarningCookieProperties();

    @NestedConfigurationProperty
    private CouchbaseServiceRegistryProperties couchbaseServiceRegistryProperties = new CouchbaseServiceRegistryProperties();

    @NestedConfigurationProperty
    private CouchbaseTicketRegistryProperties couchbaseTicketRegistryProperties = new CouchbaseTicketRegistryProperties();

    @NestedConfigurationProperty
    private EhcacheProperties ehcacheProperties = new EhcacheProperties();

    @NestedConfigurationProperty
    private AcceptAuthenticationProperties acceptAuthenticationProperties = new AcceptAuthenticationProperties();

    @NestedConfigurationProperty
    private FileAuthenticationProperties fileAuthenticationProperties = new FileAuthenticationProperties();

    @NestedConfigurationProperty
    private RejectAuthenticationProperties rejectAuthenticationProperties = new RejectAuthenticationProperties();

    @NestedConfigurationProperty
    private RemoteAddressAuthenticationProperties remoteAddressAuthenticationProperties = new RemoteAddressAuthenticationProperties();

    @NestedConfigurationProperty
    private ShiroAuthenticationProperties shiroAuthenticationProperties = new ShiroAuthenticationProperties();

    @NestedConfigurationProperty
    private MaxmindProperties maxmindProperties = new MaxmindProperties();

    @NestedConfigurationProperty
    private HazelcastProperties hazelcastProperties = new HazelcastProperties();

    @NestedConfigurationProperty
    private IgniteProperties igniteProperties = new IgniteProperties();

    @NestedConfigurationProperty
    private JaasAuthenticationProperties jaasAuthenticationProperties = new JaasAuthenticationProperties();

    @NestedConfigurationProperty
    private JdbcAuthenticationProperties jdbcAuthenticationProperties = new JdbcAuthenticationProperties();
    
    @NestedConfigurationProperty
    private DatabaseProperties databaseProperties = new DatabaseProperties();
    
    @NestedConfigurationProperty
    private JpaServiceRegistryProperties jpaServiceRegistryProperties = new JpaServiceRegistryProperties();

    @NestedConfigurationProperty
    private JpaTicketRegistryProperties jpaTicketRegistryProperties = new JpaTicketRegistryProperties();

    @NestedConfigurationProperty
    private LdapAuthorizationProperties ldapAuthorizationProperties = new LdapAuthorizationProperties();

    @NestedConfigurationProperty
    private LdapServiceRegistryProperties ldapServiceRegistryProperties = new LdapServiceRegistryProperties();

    @NestedConfigurationProperty
    private MemcachedProperties memcachedProperties = new MemcachedProperties();

    @NestedConfigurationProperty
    private MfaProperties mfaProperties = new MfaProperties();

    @NestedConfigurationProperty
    private MongoAuthenticationProperties mongoAuthenticationProperties = new MongoAuthenticationProperties();

    @NestedConfigurationProperty
    private MongoServiceRegistryProperties mongoServiceRegistryProperties = new MongoServiceRegistryProperties();

    @NestedConfigurationProperty
    private NtlmProperties ntlmProperties = new NtlmProperties();

    @NestedConfigurationProperty
    private OAuthProperties oAuthProperties = new OAuthProperties();

    @NestedConfigurationProperty
    private OidcProperties oidcProperties = new OidcProperties();

    @NestedConfigurationProperty
    private OpenIdProperties openIdProperties = new OpenIdProperties();

    @NestedConfigurationProperty
    private Pac4jProperties pac4jProperties = new Pac4jProperties();

    @NestedConfigurationProperty
    private RadiusProperties radiusProperties = new RadiusProperties();

    @NestedConfigurationProperty
    private GoogleAppsProperties googleAppsProperties = new GoogleAppsProperties();

    @NestedConfigurationProperty
    private SamlIdPProperties samlIdPProperties = new SamlIdPProperties();

    @NestedConfigurationProperty
    private SamlMetadataUIProperties samlMetadataUIProperties = new SamlMetadataUIProperties();

    @NestedConfigurationProperty
    private SamlResponseProperties samlResponseProperties = new SamlResponseProperties();

    @NestedConfigurationProperty
    private AttributeResolverProperties attributeResolverProperties = new AttributeResolverProperties();

    @NestedConfigurationProperty
    private SpnegoProperties spnegoProperties = new SpnegoProperties();

    @NestedConfigurationProperty
    private StormpathProperties stormpathProperties = new StormpathProperties();

    @NestedConfigurationProperty
    private ThemeProperties themeProperties = new ThemeProperties();

    @NestedConfigurationProperty
    private ThrottleProperties throttleProperties = new ThrottleProperties();

    @NestedConfigurationProperty
    private WsFederationProperties wsFederationProperties = new WsFederationProperties();

    @NestedConfigurationProperty
    private X509Properties x509Properties = new X509Properties();

    @NestedConfigurationProperty
    private LocaleProperties localeProperties = new LocaleProperties();

    @NestedConfigurationProperty
    private ManagementWebappProperties managementWebappProperties = new ManagementWebappProperties();

    @NestedConfigurationProperty
    private WebflowProperties webflowProperties = new WebflowProperties();
    
    public AuditProperties getAuditProperties() {
        return auditProperties;
    }

    public void setAuditProperties(final AuditProperties auditProperties) {
        this.auditProperties = auditProperties;
    }

    public AuthenticationExceptionsProperties getAuthenticationExceptionsProperties() {
        return authenticationExceptionsProperties;
    }

    public void setAuthenticationExceptionsProperties(final AuthenticationExceptionsProperties authenticationExceptionsProperties) {
        this.authenticationExceptionsProperties = authenticationExceptionsProperties;
    }

    public AuthenticationPolicyProperties getAuthenticationPolicyProperties() {
        return authenticationPolicyProperties;
    }

    public void setAuthenticationPolicyProperties(final AuthenticationPolicyProperties authenticationPolicyProperties) {
        this.authenticationPolicyProperties = authenticationPolicyProperties;
    }

    public HttpClientProperties getHttpClientProperties() {
        return httpClientProperties;
    }

    public void setHttpClientProperties(final HttpClientProperties httpClientProperties) {
        this.httpClientProperties = httpClientProperties;
    }

    public PasswordEncoderProperties getPasswordEncoderProperties() {
        return passwordEncoderProperties;
    }

    public void setPasswordEncoderProperties(final PasswordEncoderProperties passwordEncoderProperties) {
        this.passwordEncoderProperties = passwordEncoderProperties;
    }

    public PasswordPolicyProperties getPasswordPolicyProperties() {
        return passwordPolicyProperties;
    }

    public void setPasswordPolicyProperties(final PasswordPolicyProperties passwordPolicyProperties) {
        this.passwordPolicyProperties = passwordPolicyProperties;
    }

    public PersonDirPrincipalResolverProperties getPersonDirPrincipalResolverProperties() {
        return personDirPrincipalResolverProperties;
    }

    public void setPersonDirPrincipalResolverProperties(final PersonDirPrincipalResolverProperties personDirPrincipalResolverProperties) {
        this.personDirPrincipalResolverProperties = personDirPrincipalResolverProperties;
    }

    public PrincipalTransformationProperties getPrincipalTransformationProperties() {
        return principalTransformationProperties;
    }

    public void setPrincipalTransformationProperties(final PrincipalTransformationProperties principalTransformationProperties) {
        this.principalTransformationProperties = principalTransformationProperties;
    }

    public EventsProperties getEventsProperties() {
        return eventsProperties;
    }

    public void setEventsProperties(final EventsProperties eventsProperties) {
        this.eventsProperties = eventsProperties;
    }

    public HostProperties getHostProperties() {
        return hostProperties;
    }

    public void setHostProperties(final HostProperties hostProperties) {
        this.hostProperties = hostProperties;
    }

    public LogoutProperties getLogoutProperties() {
        return logoutProperties;
    }

    public void setLogoutProperties(final LogoutProperties logoutProperties) {
        this.logoutProperties = logoutProperties;
    }

    public MetricsProperties getMetricsProperties() {
        return metricsProperties;
    }

    public void setMetricsProperties(final MetricsProperties metricsProperties) {
        this.metricsProperties = metricsProperties;
    }

    public MonitorProperties getMonitorProperties() {
        return monitorProperties;
    }

    public void setMonitorProperties(final MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
    }

    public RegisteredServiceRestProperties getRegisteredServiceRestProperties() {
        return registeredServiceRestProperties;
    }

    public void setRegisteredServiceRestProperties(final RegisteredServiceRestProperties registeredServiceRestProperties) {
        this.registeredServiceRestProperties = registeredServiceRestProperties;
    }

    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    public void setServerProperties(final ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public ServiceRegistryProperties getServiceRegistryProperties() {
        return serviceRegistryProperties;
    }

    public void setServiceRegistryProperties(final ServiceRegistryProperties serviceRegistryProperties) {
        this.serviceRegistryProperties = serviceRegistryProperties;
    }

    public SloProperties getSloProperties() {
        return sloProperties;
    }

    public void setSloProperties(final SloProperties sloProperties) {
        this.sloProperties = sloProperties;
    }

    public SsoProperties getSsoProperties() {
        return ssoProperties;
    }

    public void setSsoProperties(final SsoProperties ssoProperties) {
        this.ssoProperties = ssoProperties;
    }

    public ProxyGrantingTicketProperties getProxyGrantingTicketProperties() {
        return proxyGrantingTicketProperties;
    }

    public void setProxyGrantingTicketProperties(final ProxyGrantingTicketProperties proxyGrantingTicketProperties) {
        this.proxyGrantingTicketProperties = proxyGrantingTicketProperties;
    }

    public ProxyTicketProperties getProxyTicketProperties() {
        return proxyTicketProperties;
    }

    public void setProxyTicketProperties(final ProxyTicketProperties proxyTicketProperties) {
        this.proxyTicketProperties = proxyTicketProperties;
    }

    public TicketRegistryProperties getTicketRegistryProperties() {
        return ticketRegistryProperties;
    }

    public void setTicketRegistryProperties(final TicketRegistryProperties ticketRegistryProperties) {
        this.ticketRegistryProperties = ticketRegistryProperties;
    }

    public ServiceTicketProperties getServiceTicketProperties() {
        return serviceTicketProperties;
    }

    public void setServiceTicketProperties(final ServiceTicketProperties serviceTicketProperties) {
        this.serviceTicketProperties = serviceTicketProperties;
    }

    public TicketGrantingTicketProperties getTicketGrantingTicketProperties() {
        return ticketGrantingTicketProperties;
    }

    public void setTicketGrantingTicketProperties(final TicketGrantingTicketProperties ticketGrantingTicketProperties) {
        this.ticketGrantingTicketProperties = ticketGrantingTicketProperties;
    }
    

    public TicketProperties getTicketProperties() {
        return ticketProperties;
    }

    public void setTicketProperties(final TicketProperties ticketProperties) {
        this.ticketProperties = ticketProperties;
    }

    public MessageBundleProperties getMessageBundleProperties() {
        return messageBundleProperties;
    }

    public void setMessageBundleProperties(final MessageBundleProperties messageBundleProperties) {
        this.messageBundleProperties = messageBundleProperties;
    }

    public AdminPagesSecurityProperties getAdminPagesSecurityProperties() {
        return adminPagesSecurityProperties;
    }

    public void setAdminPagesSecurityProperties(final AdminPagesSecurityProperties adminPagesSecurityProperties) {
        this.adminPagesSecurityProperties = adminPagesSecurityProperties;
    }

    public HttpWebRequestProperties getHttpWebRequestProperties() {
        return httpWebRequestProperties;
    }

    public void setHttpWebRequestProperties(final HttpWebRequestProperties httpWebRequestProperties) {
        this.httpWebRequestProperties = httpWebRequestProperties;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(final ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    public GoogleAnalyticsProperties getGoogleAnalyticsProperties() {
        return googleAnalyticsProperties;
    }

    public void setGoogleAnalyticsProperties(final GoogleAnalyticsProperties googleAnalyticsProperties) {
        this.googleAnalyticsProperties = googleAnalyticsProperties;
    }

    public AcceptableUsagePolicyProperties getAcceptableUsagePolicyProperties() {
        return acceptableUsagePolicyProperties;
    }

    public void setAcceptableUsagePolicyProperties(final AcceptableUsagePolicyProperties acceptableUsagePolicyProperties) {
        this.acceptableUsagePolicyProperties = acceptableUsagePolicyProperties;
    }

    public ClearpassProperties getClearpassProperties() {
        return clearpassProperties;
    }

    public void setClearpassProperties(final ClearpassProperties clearpassProperties) {
        this.clearpassProperties = clearpassProperties;
    }
    

    public TicketGrantingCookieProperties getTicketGrantingCookieProperties() {
        return ticketGrantingCookieProperties;
    }

    public void setTicketGrantingCookieProperties(final TicketGrantingCookieProperties ticketGrantingCookieProperties) {
        this.ticketGrantingCookieProperties = ticketGrantingCookieProperties;
    }

    public WarningCookieProperties getWarningCookieProperties() {
        return warningCookieProperties;
    }

    public void setWarningCookieProperties(final WarningCookieProperties warningCookieProperties) {
        this.warningCookieProperties = warningCookieProperties;
    }

    public CouchbaseServiceRegistryProperties getCouchbaseServiceRegistryProperties() {
        return couchbaseServiceRegistryProperties;
    }

    public void setCouchbaseServiceRegistryProperties(final CouchbaseServiceRegistryProperties couchbaseServiceRegistryProperties) {
        this.couchbaseServiceRegistryProperties = couchbaseServiceRegistryProperties;
    }

    public CouchbaseTicketRegistryProperties getCouchbaseTicketRegistryProperties() {
        return couchbaseTicketRegistryProperties;
    }

    public void setCouchbaseTicketRegistryProperties(final CouchbaseTicketRegistryProperties couchbaseTicketRegistryProperties) {
        this.couchbaseTicketRegistryProperties = couchbaseTicketRegistryProperties;
    }

    public EhcacheProperties getEhcacheProperties() {
        return ehcacheProperties;
    }

    public void setEhcacheProperties(final EhcacheProperties ehcacheProperties) {
        this.ehcacheProperties = ehcacheProperties;
    }

    public AcceptAuthenticationProperties getAcceptAuthenticationProperties() {
        return acceptAuthenticationProperties;
    }

    public void setAcceptAuthenticationProperties(final AcceptAuthenticationProperties acceptAuthenticationProperties) {
        this.acceptAuthenticationProperties = acceptAuthenticationProperties;
    }

    public FileAuthenticationProperties getFileAuthenticationProperties() {
        return fileAuthenticationProperties;
    }

    public void setFileAuthenticationProperties(final FileAuthenticationProperties fileAuthenticationProperties) {
        this.fileAuthenticationProperties = fileAuthenticationProperties;
    }

    public RejectAuthenticationProperties getRejectAuthenticationProperties() {
        return rejectAuthenticationProperties;
    }

    public void setRejectAuthenticationProperties(final RejectAuthenticationProperties rejectAuthenticationProperties) {
        this.rejectAuthenticationProperties = rejectAuthenticationProperties;
    }

    public RemoteAddressAuthenticationProperties getRemoteAddressAuthenticationProperties() {
        return remoteAddressAuthenticationProperties;
    }

    public void setRemoteAddressAuthenticationProperties(final RemoteAddressAuthenticationProperties remoteAddressAuthenticationProperties) {
        this.remoteAddressAuthenticationProperties = remoteAddressAuthenticationProperties;
    }

    public ShiroAuthenticationProperties getShiroAuthenticationProperties() {
        return shiroAuthenticationProperties;
    }

    public void setShiroAuthenticationProperties(final ShiroAuthenticationProperties shiroAuthenticationProperties) {
        this.shiroAuthenticationProperties = shiroAuthenticationProperties;
    }

    public MaxmindProperties getMaxmindProperties() {
        return maxmindProperties;
    }

    public void setMaxmindProperties(final MaxmindProperties maxmindProperties) {
        this.maxmindProperties = maxmindProperties;
    }

    public HazelcastProperties getHazelcastProperties() {
        return hazelcastProperties;
    }

    public void setHazelcastProperties(final HazelcastProperties hazelcastProperties) {
        this.hazelcastProperties = hazelcastProperties;
    }

    public IgniteProperties getIgniteProperties() {
        return igniteProperties;
    }

    public void setIgniteProperties(final IgniteProperties igniteProperties) {
        this.igniteProperties = igniteProperties;
    }

    public JaasAuthenticationProperties getJaasAuthenticationProperties() {
        return jaasAuthenticationProperties;
    }

    public void setJaasAuthenticationProperties(final JaasAuthenticationProperties jaasAuthenticationProperties) {
        this.jaasAuthenticationProperties = jaasAuthenticationProperties;
    }

    public JdbcAuthenticationProperties getJdbcAuthenticationProperties() {
        return jdbcAuthenticationProperties;
    }

    public void setJdbcAuthenticationProperties(final JdbcAuthenticationProperties jdbcAuthenticationProperties) {
        this.jdbcAuthenticationProperties = jdbcAuthenticationProperties;
    }
    

    public DatabaseProperties getDatabaseProperties() {
        return databaseProperties;
    }

    public void setDatabaseProperties(final DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    public JpaServiceRegistryProperties getJpaServiceRegistryProperties() {
        return jpaServiceRegistryProperties;
    }

    public void setJpaServiceRegistryProperties(final JpaServiceRegistryProperties jpaServiceRegistryProperties) {
        this.jpaServiceRegistryProperties = jpaServiceRegistryProperties;
    }

    public JpaTicketRegistryProperties getJpaTicketRegistryProperties() {
        return jpaTicketRegistryProperties;
    }

    public void setJpaTicketRegistryProperties(final JpaTicketRegistryProperties jpaTicketRegistryProperties) {
        this.jpaTicketRegistryProperties = jpaTicketRegistryProperties;
    }

    public LdapAuthorizationProperties getLdapAuthorizationProperties() {
        return ldapAuthorizationProperties;
    }

    public void setLdapAuthorizationProperties(final LdapAuthorizationProperties ldapAuthorizationProperties) {
        this.ldapAuthorizationProperties = ldapAuthorizationProperties;
    }

    public LdapServiceRegistryProperties getLdapServiceRegistryProperties() {
        return ldapServiceRegistryProperties;
    }

    public void setLdapServiceRegistryProperties(final LdapServiceRegistryProperties ldapServiceRegistryProperties) {
        this.ldapServiceRegistryProperties = ldapServiceRegistryProperties;
    }

    public MemcachedProperties getMemcachedProperties() {
        return memcachedProperties;
    }

    public void setMemcachedProperties(final MemcachedProperties memcachedProperties) {
        this.memcachedProperties = memcachedProperties;
    }

    public MfaProperties getMfaProperties() {
        return mfaProperties;
    }

    public void setMfaProperties(final MfaProperties mfaProperties) {
        this.mfaProperties = mfaProperties;
    }

    public MongoAuthenticationProperties getMongoAuthenticationProperties() {
        return mongoAuthenticationProperties;
    }

    public void setMongoAuthenticationProperties(final MongoAuthenticationProperties mongoAuthenticationProperties) {
        this.mongoAuthenticationProperties = mongoAuthenticationProperties;
    }

    public MongoServiceRegistryProperties getMongoServiceRegistryProperties() {
        return mongoServiceRegistryProperties;
    }

    public void setMongoServiceRegistryProperties(final MongoServiceRegistryProperties mongoServiceRegistryProperties) {
        this.mongoServiceRegistryProperties = mongoServiceRegistryProperties;
    }

    public NtlmProperties getNtlmProperties() {
        return ntlmProperties;
    }

    public void setNtlmProperties(final NtlmProperties ntlmProperties) {
        this.ntlmProperties = ntlmProperties;
    }

    public OAuthProperties getoAuthProperties() {
        return oAuthProperties;
    }

    public void setoAuthProperties(final OAuthProperties oAuthProperties) {
        this.oAuthProperties = oAuthProperties;
    }

    public OidcProperties getOidcProperties() {
        return oidcProperties;
    }

    public void setOidcProperties(final OidcProperties oidcProperties) {
        this.oidcProperties = oidcProperties;
    }

    public OpenIdProperties getOpenIdProperties() {
        return openIdProperties;
    }

    public void setOpenIdProperties(final OpenIdProperties openIdProperties) {
        this.openIdProperties = openIdProperties;
    }

    public Pac4jProperties getPac4jProperties() {
        return pac4jProperties;
    }

    public void setPac4jProperties(final Pac4jProperties pac4jProperties) {
        this.pac4jProperties = pac4jProperties;
    }

    public RadiusProperties getRadiusProperties() {
        return radiusProperties;
    }

    public void setRadiusProperties(final RadiusProperties radiusProperties) {
        this.radiusProperties = radiusProperties;
    }

    public GoogleAppsProperties getGoogleAppsProperties() {
        return googleAppsProperties;
    }

    public void setGoogleAppsProperties(final GoogleAppsProperties googleAppsProperties) {
        this.googleAppsProperties = googleAppsProperties;
    }

    public SamlIdPProperties getSamlIdPProperties() {
        return samlIdPProperties;
    }

    public void setSamlIdPProperties(final SamlIdPProperties samlIdPProperties) {
        this.samlIdPProperties = samlIdPProperties;
    }

    public SamlMetadataUIProperties getSamlMetadataUIProperties() {
        return samlMetadataUIProperties;
    }

    public void setSamlMetadataUIProperties(final SamlMetadataUIProperties samlMetadataUIProperties) {
        this.samlMetadataUIProperties = samlMetadataUIProperties;
    }

    public SamlResponseProperties getSamlResponseProperties() {
        return samlResponseProperties;
    }

    public void setSamlResponseProperties(final SamlResponseProperties samlResponseProperties) {
        this.samlResponseProperties = samlResponseProperties;
    }

    public AttributeResolverProperties getAttributeResolverProperties() {
        return attributeResolverProperties;
    }

    public void setAttributeResolverProperties(final AttributeResolverProperties attributeResolverProperties) {
        this.attributeResolverProperties = attributeResolverProperties;
    }

    public SpnegoProperties getSpnegoProperties() {
        return spnegoProperties;
    }

    public void setSpnegoProperties(final SpnegoProperties spnegoProperties) {
        this.spnegoProperties = spnegoProperties;
    }

    public StormpathProperties getStormpathProperties() {
        return stormpathProperties;
    }

    public void setStormpathProperties(final StormpathProperties stormpathProperties) {
        this.stormpathProperties = stormpathProperties;
    }

    public ThemeProperties getThemeProperties() {
        return themeProperties;
    }

    public void setThemeProperties(final ThemeProperties themeProperties) {
        this.themeProperties = themeProperties;
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(final ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }

    public WsFederationProperties getWsFederationProperties() {
        return wsFederationProperties;
    }

    public void setWsFederationProperties(final WsFederationProperties wsFederationProperties) {
        this.wsFederationProperties = wsFederationProperties;
    }

    public X509Properties getX509Properties() {
        return x509Properties;
    }

    public void setX509Properties(final X509Properties x509Properties) {
        this.x509Properties = x509Properties;
    }

    public LocaleProperties getLocaleProperties() {
        return localeProperties;
    }

    public void setLocaleProperties(final LocaleProperties localeProperties) {
        this.localeProperties = localeProperties;
    }

    public ManagementWebappProperties getManagementWebappProperties() {
        return managementWebappProperties;
    }

    public void setManagementWebappProperties(final ManagementWebappProperties managementWebappProperties) {
        this.managementWebappProperties = managementWebappProperties;
    }

    public WebflowProperties getWebflowProperties() {
        return webflowProperties;
    }

    public void setWebflowProperties(final WebflowProperties webflowProperties) {
        this.webflowProperties = webflowProperties;
    }
}
