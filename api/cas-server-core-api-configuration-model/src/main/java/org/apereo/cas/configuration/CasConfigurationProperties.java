package org.apereo.cas.configuration;

import org.apereo.cas.configuration.model.core.CasServerCoreProperties;
import org.apereo.cas.configuration.model.core.CasServerHostProperties;
import org.apereo.cas.configuration.model.core.CasServerProperties;
import org.apereo.cas.configuration.model.core.audit.AuditProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authz.AccessStrategyProperties;
import org.apereo.cas.configuration.model.core.config.cloud.SpringCloudConfigurationProperties;
import org.apereo.cas.configuration.model.core.config.standalone.StandaloneConfigurationProperties;
import org.apereo.cas.configuration.model.core.events.EventsProperties;
import org.apereo.cas.configuration.model.core.logging.LoggingProperties;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.configuration.model.core.rest.RestProperties;
import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.configuration.model.core.slo.SingleLogoutProperties;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.configuration.model.core.util.TicketProperties;
import org.apereo.cas.configuration.model.core.web.LocaleProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowProperties;
import org.apereo.cas.configuration.model.core.web.security.HttpRequestProperties;
import org.apereo.cas.configuration.model.core.web.view.ViewProperties;
import org.apereo.cas.configuration.model.support.account.AccountManagementRegistrationProperties;
import org.apereo.cas.configuration.model.support.acme.AcmeProperties;
import org.apereo.cas.configuration.model.support.analytics.GoogleAnalyticsProperties;
import org.apereo.cas.configuration.model.support.apn.APNMessagingProperties;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.aws.AmazonSecurityTokenServiceProperties;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.configuration.model.support.clearpass.ClearpassProperties;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.configuration.model.support.cookie.WarningCookieProperties;
import org.apereo.cas.configuration.model.support.custom.CasCustomProperties;
import org.apereo.cas.configuration.model.support.email.EmailProvidersProperties;
import org.apereo.cas.configuration.model.support.firebase.GoogleFirebaseCloudMessagingProperties;
import org.apereo.cas.configuration.model.support.geo.GeoLocationProperties;
import org.apereo.cas.configuration.model.support.heimdall.HeimdallAuthorizationProperties;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.configuration.model.support.javers.JaversProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.multitenancy.MultitenancyProperties;
import org.apereo.cas.configuration.model.support.saml.SamlCoreProperties;
import org.apereo.cas.configuration.model.support.saml.mdui.SamlMetadataUIProperties;
import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.configuration.model.support.scim.ScimProvisioningProperties;
import org.apereo.cas.configuration.model.support.slack.SlackMessagingProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProvidersProperties;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link CasConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas")
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-api", automated = true)
public class CasConfigurationProperties implements Serializable {
    /**
     * Prefix used for all CAS-specific settings.
     */
    public static final String PREFIX = "cas";

    @Serial
    private static final long serialVersionUID = -8620267783496071683L;

    /**
     * Timestamp that indicates the initialization time.
     */
    private long initializationTime = Instant.now(Clock.systemUTC()).toEpochMilli();

    /**
     * Logging functionality.
     */
    @NestedConfigurationProperty
    private LoggingProperties logging = new LoggingProperties();

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
     * Service access Strategy and authorization-related functionality.
     */
    @NestedConfigurationProperty
    private AccessStrategyProperties accessStrategy = new AccessStrategyProperties();

    /**
     * Heimdall authorization-related functionality.
     */
    @NestedConfigurationProperty
    private HeimdallAuthorizationProperties heimdall = new HeimdallAuthorizationProperties();

    /**
     * ACME functionality.
     */
    @NestedConfigurationProperty
    private AcmeProperties acme = new AcmeProperties();

    /**
     * SCIM functionality.
     */
    @NestedConfigurationProperty
    private ScimProvisioningProperties scim = new ScimProvisioningProperties();

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
     * Monitoring functionality.
     */
    @NestedConfigurationProperty
    private MonitorProperties monitor = new MonitorProperties();

    /**
     * Settings that define this CAS host.
     */
    @NestedConfigurationProperty
    private CasServerHostProperties host = new CasServerHostProperties();

    /**
     * Logout functionality.
     */
    @NestedConfigurationProperty
    private LogoutProperties logout = new LogoutProperties();

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
    private SingleLogoutProperties slo = new SingleLogoutProperties();

    /**
     * SSO functionality.
     */
    @NestedConfigurationProperty
    private SingleSignOnProperties sso = new SingleSignOnProperties();

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
     * Settings that control filtering of the incoming http requests.
     */
    @NestedConfigurationProperty
    private HttpRequestProperties httpWebRequest = new HttpRequestProperties();

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
     * Google Firebase Cloud Messaging functionality.
     */
    @NestedConfigurationProperty
    private GoogleFirebaseCloudMessagingProperties googleFirebaseMessaging = new GoogleFirebaseCloudMessagingProperties();

    /**
     * Apple Push Notification functionality.
     */
    @NestedConfigurationProperty
    private APNMessagingProperties apnMessaging = new APNMessagingProperties();

    /**
     * Slack Messaging functionality.
     */
    @NestedConfigurationProperty
    private SlackMessagingProperties slackMessaging = new SlackMessagingProperties();

    /**
     * Google reCAPTCHA settings.
     */
    @NestedConfigurationProperty
    private GoogleRecaptchaProperties googleRecaptcha = new GoogleRecaptchaProperties();

    /**
     * SMS and Text messaging settings.
     */
    @NestedConfigurationProperty
    private SmsProvidersProperties smsProvider = new SmsProvidersProperties();

    /**
     * Email settings.
     */
    @NestedConfigurationProperty
    private EmailProvidersProperties emailProvider = new EmailProvidersProperties();

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
     * GeoLocation settings.
     */
    @NestedConfigurationProperty
    private GeoLocationProperties geoLocation = new GeoLocationProperties();

    /**
     * SAML SP integration settings.
     */
    @NestedConfigurationProperty
    private SamlServiceProviderProperties samlSp = new SamlServiceProviderProperties();

    /**
     * General database and hibernate settings.
     */
    @NestedConfigurationProperty
    private DatabaseProperties jdbc = new DatabaseProperties();

    /**
     * Integration settings for amazon sts.
     */
    @NestedConfigurationProperty
    private AmazonSecurityTokenServiceProperties amazonSts = new AmazonSecurityTokenServiceProperties();

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
     * Spring Webflow functionality.
     */
    @NestedConfigurationProperty
    private WebflowProperties webflow = new WebflowProperties();

    /**
     * Custom properties.
     */
    @NestedConfigurationProperty
    private CasCustomProperties custom = new CasCustomProperties();

    /**
     * Standalone configuration settings.
     */
    @NestedConfigurationProperty
    private StandaloneConfigurationProperties standalone = new StandaloneConfigurationProperties();

    /**
     * Spring cloud configuration settings.
     */
    @NestedConfigurationProperty
    private SpringCloudConfigurationProperties spring = new SpringCloudConfigurationProperties();

    /**
     * Account registration settings.
     */
    @NestedConfigurationProperty
    private AccountManagementRegistrationProperties accountRegistration = new AccountManagementRegistrationProperties();

    /**
     * Core internal settings.
     */
    @NestedConfigurationProperty
    private CasServerCoreProperties core = new CasServerCoreProperties();

    /**
     * Javers settings.
     */
    @NestedConfigurationProperty
    private JaversProperties javers = new JaversProperties();

    /**
     * Multitenancy settings.
     */
    @NestedConfigurationProperty
    private MultitenancyProperties multitenancy = new MultitenancyProperties();

    /**
     * Bind from map.
     *
     * @param payload the payload
     * @return the optional
     */
    public static Optional<CasConfigurationProperties> bindFrom(final Map<String, Object> payload) {
        return bindFrom(payload, CasConfigurationProperties.class);
    }

    /**
     * Bind from map.
     *
     * @param <T>     the type parameter
     * @param payload the payload
     * @param clazz   the clazz
     * @return the optional
     */
    public static <T> Optional<T> bindFrom(final Map<String, Object> payload,
                                           final Class<T> clazz) {
        if (!payload.isEmpty()) {
            val annotation = Objects.requireNonNull(clazz.getAnnotation(ConfigurationProperties.class));
            val name = StringUtils.defaultIfBlank(annotation.prefix(), annotation.value());
            val binder = new Binder(ConfigurationPropertySources.from(new MapPropertySource(clazz.getSimpleName(), payload)));
            val bound = binder.bind(name, Bindable.of(clazz));
            if (bound.isBound()) {
                return Optional.of(bound.get());
            }
        }
        return Optional.empty();
    }
}
