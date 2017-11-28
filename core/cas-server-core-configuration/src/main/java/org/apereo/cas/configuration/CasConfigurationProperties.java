package org.apereo.cas.configuration;

import org.apereo.cas.configuration.model.core.CasServerProperties;
import org.apereo.cas.configuration.model.core.HostProperties;
import org.apereo.cas.configuration.model.core.audit.AuditProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.events.EventsProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.configuration.model.core.metrics.MetricsProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.configuration.model.core.rest.RestProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.configuration.model.core.slo.SloProperties;
import org.apereo.cas.configuration.model.core.sso.SsoProperties;
import org.apereo.cas.configuration.model.core.util.TicketProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.model.core.web.security.HttpWebRequestProperties;
import org.apereo.cas.configuration.model.core.web.view.ViewProperties;
import org.apereo.cas.configuration.model.support.analytics.GoogleAnalyticsProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.clearpass.ClearpassProperties;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.configuration.model.support.geo.googlemaps.GoogleMapsProperties;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.saml.SamlCoreProperties;
import org.apereo.cas.configuration.model.support.saml.googleapps.GoogleAppsProperties;
import org.apereo.cas.configuration.model.support.saml.mdui.SamlMetadataUIProperties;
import org.apereo.cas.configuration.model.support.saml.shibboleth.ShibbolethAttributeResolverProperties;
import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.configuration.model.support.scim.ScimProperties;
import org.apereo.cas.configuration.model.support.sms.ClickatellProperties;
import org.apereo.cas.configuration.model.support.sms.TextMagicProperties;
import org.apereo.cas.configuration.model.support.sms.TwilioProperties;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.configuration.model.webapp.LocaleProperties;
import org.apereo.cas.configuration.model.webapp.WebflowProperties;
import org.apereo.cas.configuration.model.webapp.mgmt.ManagementWebappProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link CasConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(value = "cas")
public class CasConfigurationProperties implements Serializable {
    /**
     * Prefix used for all CAS-specific settings.
     */
    public static final String PREFIX = "cas";

    private static final long serialVersionUID = -8620267783496071683L;

    /**
     * Interrupt/notification functionality.
     */
    @NestedConfigurationProperty
    private InterruptProperties interrupt = new InterruptProperties();

    /**
     * Attribute consent functionality.
     */
    @NestedConfigurationProperty
    private ConsentProperties consent = new ConsentProperties();

    /**
     * SCIM functionality.
     */
    @NestedConfigurationProperty
    private ScimProperties scim = new ScimProperties();

    /**
     * General settings for authentication.
     */
    @NestedConfigurationProperty
    private AuthenticationProperties authn = new AuthenticationProperties();

    /**
     * Authentication audit functionality.
     */
    @NestedConfigurationProperty
    private AuditProperties audit = new AuditProperties();

    /**
     * Http client and outgoing connections settings.
     */
    @NestedConfigurationProperty
    private HttpClientProperties httpClient = new HttpClientProperties();

    /**
     * Person directory and principal resolution functionality.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties personDirectory = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Authentication events functionality.
     */
    @NestedConfigurationProperty
    private EventsProperties events = new EventsProperties();

    /**
     * Settings that define this CAS host.
     */
    @NestedConfigurationProperty
    private HostProperties host = new HostProperties();

    /**
     * Logout functionality.
     */
    @NestedConfigurationProperty
    private LogoutProperties logout = new LogoutProperties();

    /**
     * Metrics functionality.
     */
    @NestedConfigurationProperty
    private MetricsProperties metrics = new MetricsProperties();

    /**
     * Monitoring functionality.
     */
    @NestedConfigurationProperty
    private MonitorProperties monitor = new MonitorProperties();

    /**
     * REST API functionality.
     */
    @NestedConfigurationProperty
    private RestProperties rest = new RestProperties();

    /**
     * Settings that define this CAS server instance.
     */
    @NestedConfigurationProperty
    private CasServerProperties server = new CasServerProperties();

    /**
     * Service registry functionality.
     */
    @NestedConfigurationProperty
    private ServiceRegistryProperties serviceRegistry = new ServiceRegistryProperties();

    /**
     * SLO functionality.
     */
    @NestedConfigurationProperty
    private SloProperties slo = new SloProperties();

    /**
     * SSO functionality.
     */
    @NestedConfigurationProperty
    private SsoProperties sso = new SsoProperties();

    /**
     * Ticketing functionality.
     */
    @NestedConfigurationProperty
    private TicketProperties ticket = new TicketProperties();

    /**
     * Message bundles and internationalization functionality.
     */
    @NestedConfigurationProperty
    private MessageBundleProperties messageBundle = new MessageBundleProperties();

    /**
     * Admin pages and their security, controling endpoints, etc.
     */
    @NestedConfigurationProperty
    private AdminPagesSecurityProperties adminPagesSecurity = new AdminPagesSecurityProperties();

    /**
     * Settings that control filtering of the incoming http requests.
     */
    @NestedConfigurationProperty
    private HttpWebRequestProperties httpWebRequest = new HttpWebRequestProperties();

    /**
     * Views and UI functionality.
     */
    @NestedConfigurationProperty
    private ViewProperties view = new ViewProperties();

    /**
     * Google Analytics functionality.
     */
    @NestedConfigurationProperty
    private GoogleAnalyticsProperties googleAnalytics = new GoogleAnalyticsProperties();

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    /**
     * Twilio settings.
     */
    @NestedConfigurationProperty
    private TwilioProperties twilio = new TwilioProperties();

    /**
     * TextMagic settings.
     */
    @NestedConfigurationProperty
    private TextMagicProperties textMagic = new TextMagicProperties();

    /**
     * Clickatell settings.
     */
    @NestedConfigurationProperty
    private ClickatellProperties clickatell = new ClickatellProperties();

    /**
     * AUP settings.
     */
    @NestedConfigurationProperty
    private AcceptableUsagePolicyProperties acceptableUsagePolicy = new AcceptableUsagePolicyProperties();

    /**
     * Clearpass settings.
     */
    @NestedConfigurationProperty
    private ClearpassProperties clearpass = new ClearpassProperties();

    /**
     * Ticket-granting cookie settings.
     */
    @NestedConfigurationProperty
    private TicketGrantingCookieProperties tgc = new TicketGrantingCookieProperties();

    /**
     * Warning cookie settings.
     */
    @NestedConfigurationProperty
    private WarningCookieProperties warningCookie = new WarningCookieProperties();

    /**
     * SAML SP integration settings.
     */
    @NestedConfigurationProperty
    private SamlServiceProviderProperties samlSp = new SamlServiceProviderProperties();

    /**
     * MaxMind settings.
     */
    @NestedConfigurationProperty
    private MaxmindProperties maxmind = new MaxmindProperties();

    /**
     * Google Maps settings.
     */
    @NestedConfigurationProperty
    private GoogleMapsProperties googleMaps = new GoogleMapsProperties();

    /**
     * General database and hibernate settings.
     */
    @NestedConfigurationProperty
    private DatabaseProperties jdbc = new DatabaseProperties();

    /**
     * Google Apps integration settings.
     */
    @NestedConfigurationProperty
    private GoogleAppsProperties googleApps = new GoogleAppsProperties();

    /**
     * SAML Metadata UI settings and parsing.
     */
    @NestedConfigurationProperty
    private SamlMetadataUIProperties samlMetadataUi = new SamlMetadataUIProperties();

    /**
     * SAML Core functionality and settings.
     */
    @NestedConfigurationProperty
    private SamlCoreProperties samlCore = new SamlCoreProperties();

    /**
     * Shibboleth attribute resolution settings.
     */
    @NestedConfigurationProperty
    private ShibbolethAttributeResolverProperties shibAttributeResolver = new ShibbolethAttributeResolverProperties();

    /**
     * UI and theme settings.
     */
    @NestedConfigurationProperty
    private ThemeProperties theme = new ThemeProperties();

    /**
     * Locale and internationalization settings.
     */
    @NestedConfigurationProperty
    private LocaleProperties locale = new LocaleProperties();

    /**
     * CAS Management Webapp functionality.
     */
    @NestedConfigurationProperty
    private ManagementWebappProperties mgmt = new ManagementWebappProperties();

    /**
     * Spring Webflow functionality.
     */
    @NestedConfigurationProperty
    private WebflowProperties webflow = new WebflowProperties();

    public ConsentProperties getConsent() {
        return consent;
    }

    public void setConsent(final ConsentProperties consent) {
        this.consent = consent;
    }

    public AuditProperties getAudit() {
        return audit;
    }

    public void setAudit(final AuditProperties audit) {
        this.audit = audit;
    }

    public HttpClientProperties getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(final HttpClientProperties httpClient) {
        this.httpClient = httpClient;
    }

    public PersonDirectoryPrincipalResolverProperties getPersonDirectory() {
        return personDirectory;
    }

    public void setPersonDirectory(final PersonDirectoryPrincipalResolverProperties personDirectory) {
        this.personDirectory = personDirectory;
    }

    public RestProperties getRest() {
        return rest;
    }

    public void setRest(final RestProperties rest) {
        this.rest = rest;
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

    public CasServerProperties getServer() {
        return server;
    }

    public void setServer(final CasServerProperties server) {
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

    public MaxmindProperties getMaxmind() {
        return maxmind;
    }

    public void setMaxmind(final MaxmindProperties maxmind) {
        this.maxmind = maxmind;
    }

    public DatabaseProperties getJdbc() {
        return jdbc;
    }

    public void setJdbc(final DatabaseProperties jdbc) {
        this.jdbc = jdbc;
    }

    public GoogleAppsProperties getGoogleApps() {
        return googleApps;
    }

    public void setGoogleApps(final GoogleAppsProperties googleApps) {
        this.googleApps = googleApps;
    }

    public SamlMetadataUIProperties getSamlMetadataUi() {
        return samlMetadataUi;
    }

    public void setSamlMetadataUi(final SamlMetadataUIProperties samlMetadataUi) {
        this.samlMetadataUi = samlMetadataUi;
    }

    public SamlCoreProperties getSamlCore() {
        return samlCore;
    }

    public void setSamlCore(final SamlCoreProperties samlCore) {
        this.samlCore = samlCore;
    }

    public ShibbolethAttributeResolverProperties getShibAttributeResolver() {
        return shibAttributeResolver;
    }

    public void setShibAttributeResolver(final ShibbolethAttributeResolverProperties shibAttributeResolver) {
        this.shibAttributeResolver = shibAttributeResolver;
    }

    public ThemeProperties getTheme() {
        return theme;
    }

    public void setTheme(final ThemeProperties theme) {
        this.theme = theme;
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

    public AuthenticationProperties getAuthn() {
        return authn;
    }

    public void setAuthn(final AuthenticationProperties authn) {
        this.authn = authn;
    }

    public GoogleMapsProperties getGoogleMaps() {
        return googleMaps;
    }

    public void setGoogleMaps(final GoogleMapsProperties googleMaps) {
        this.googleMaps = googleMaps;
    }

    public GoogleRecaptchaProperties getGoogleRecaptcha() {
        return googleRecaptcha;
    }

    public void setGoogleRecaptcha(final GoogleRecaptchaProperties googleRecaptcha) {
        this.googleRecaptcha = googleRecaptcha;
    }

    public SamlServiceProviderProperties getSamlSp() {
        return samlSp;
    }

    public void setSamlSp(final SamlServiceProviderProperties samlSp) {
        this.samlSp = samlSp;
    }

    public TwilioProperties getTwilio() {
        return twilio;
    }

    public void setTwilio(final TwilioProperties twilio) {
        this.twilio = twilio;
    }

    public TextMagicProperties getTextMagic() {
        return textMagic;
    }

    public void setTextMagic(final TextMagicProperties textMagic) {
        this.textMagic = textMagic;
    }

    public ScimProperties getScim() {
        return scim;
    }

    public void setScim(final ScimProperties scim) {
        this.scim = scim;
    }

    public ClickatellProperties getClickatell() {
        return clickatell;
    }

    public void setClickatell(final ClickatellProperties clickatell) {
        this.clickatell = clickatell;
    }

    public InterruptProperties getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(final InterruptProperties interrupt) {
        this.interrupt = interrupt;
    }
}
