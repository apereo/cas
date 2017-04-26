package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AllAuthenticationPolicy;
import org.apereo.cas.authentication.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactory;
import org.apereo.cas.authentication.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.DefaultAdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.BasicPrincipalResolver;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.web.flow.AuthenticationExceptionHandler;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CasCoreAuthenticationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreAuthenticationConfiguration.class);

    private static final String BEAN_NAME_HTTP_CLIENT = "supportsTrustStoreSslSocketFactoryHttpClient";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("geoLocationService")
    private GeoLocationService geoLocationService;

    @Autowired(required = false)
    @Qualifier("acceptPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration acceptPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("jaasPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Bean
    public PrincipalFactory jaasPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationExceptionHandler authenticationExceptionHandler() {
        final AuthenticationExceptionHandler h = new AuthenticationExceptionHandler();
        h.setErrors(casProperties.getAuthn().getExceptions().getExceptions());
        return h;
    }

    @Bean
    public AuthenticationPolicy authenticationPolicy() {
        if (casProperties.getAuthn().getPolicy().getReq().isEnabled()) {
            final RequiredHandlerAuthenticationPolicy bean =
                    new RequiredHandlerAuthenticationPolicy(casProperties.getAuthn().getPolicy().getReq().getHandlerName());
            bean.setTryAll(casProperties.getAuthn().getPolicy().getReq().isTryAll());
            return bean;
        }

        if (casProperties.getAuthn().getPolicy().getAll().isEnabled()) {
            return new AllAuthenticationPolicy();
        }

        if (casProperties.getAuthn().getPolicy().getNotPrevented().isEnabled()) {
            return new NotPreventedAuthenticationPolicy();
        }

        return new AnyAuthenticationPolicy(casProperties.getAuthn().getPolicy().getAny().isTryAll());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        final Pattern pattern = Pattern.compile("::");
        final AcceptUsersAuthenticationHandler h = new AcceptUsersAuthenticationHandler();
        if (StringUtils.isNotBlank(casProperties.getAuthn().getAccept().getUsers())
                && casProperties.getAuthn().getAccept().getUsers().contains(pattern.pattern())) {
            final Set<String> usersPasswords =
                    org.springframework.util.StringUtils.commaDelimitedListToSet(
                            casProperties.getAuthn().getAccept().getUsers());
            final Map<String, String> parsedUsers = new HashMap<>();
            usersPasswords.stream().forEach(usersPassword -> {
                final String[] splitArray = pattern.split(usersPassword);
                parsedUsers.put(splitArray[0], splitArray[1]);
            });
            h.setUsers(parsedUsers);
        }
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getAccept().getPasswordEncoder()));
        if (acceptPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(acceptPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getAccept().getPrincipalTransformation()));
        h.setPrincipalFactory(acceptUsersPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public PrincipalFactory acceptUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationContextValidator authenticationContextValidator() {
        final AuthenticationContextValidator val = new AuthenticationContextValidator();
        val.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        val.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        return val;
    }

    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport(@Qualifier(BEAN_NAME_HTTP_CLIENT) final HttpClient httpClient) {
        final DefaultAuthenticationSystemSupport r = new DefaultAuthenticationSystemSupport();
        r.setAuthenticationTransactionManager(defaultAuthenticationTransactionManager(httpClient));
        r.setPrincipalElectionStrategy(defaultPrincipalElectionStrategy());
        return r;
    }

    @Bean(name = {"defaultAuthenticationTransactionManager", "authenticationTransactionManager"})
    public AuthenticationTransactionManager defaultAuthenticationTransactionManager(@Qualifier(BEAN_NAME_HTTP_CLIENT) final HttpClient httpClient) {
        final DefaultAuthenticationTransactionManager r = new DefaultAuthenticationTransactionManager();
        r.setAuthenticationManager(authenticationManager(httpClient));
        return r;
    }

    @Bean(name = {"defaultPrincipalElectionStrategy", "principalElectionStrategy"})
    public PrincipalElectionStrategy defaultPrincipalElectionStrategy() {
        final DefaultPrincipalElectionStrategy s = new DefaultPrincipalElectionStrategy();
        s.setPrincipalFactory(principalFactory());
        return s;
    }

    @RefreshScope
    @Bean
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() {
        return new FileTrustStoreSslSocketFactory(casProperties.getHttpClient().getTruststore().getFile(),
                casProperties.getHttpClient().getTruststore().getPsw());
    }

    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }

    @Bean
    public List authenticationMetadataPopulators() {
        final List list = new ArrayList<>();
        list.add(successfulHandlerMetaDataPopulator());
        list.add(rememberMeAuthenticationMetaDataPopulator());

        if (casProperties.getClearpass().isCacheCredential()) {
            list.add(new CacheCredentialsMetaDataPopulator());
        }
        return list;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            @Qualifier(BEAN_NAME_HTTP_CLIENT) final HttpClient httpClient) {
        final PolicyBasedAuthenticationManager p = new PolicyBasedAuthenticationManager();

        p.setAuthenticationMetaDataPopulators(authenticationMetadataPopulators());
        p.setHandlerResolverMap(authenticationHandlersResolvers(httpClient));
        p.setAuthenticationHandlerResolver(registeredServiceAuthenticationHandlerResolver());
        p.setAuthenticationPolicy(authenticationPolicy());
        return p;
    }

    @Bean
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        final RegisteredServiceAuthenticationHandlerResolver r =
                new RegisteredServiceAuthenticationHandlerResolver();
        r.setServicesManager(servicesManager);
        return r;
    }

    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }

    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }

    @RefreshScope
    @Bean
    public PrincipalResolver personDirectoryPrincipalResolver() {
        final PersonDirectoryPrincipalResolver bean = new PersonDirectoryPrincipalResolver();
        bean.setAttributeRepository(this.attributeRepository);
        bean.setPrincipalAttributeName(casProperties.getPersonDirectory().getPrincipalAttribute());
        bean.setReturnNullIfNoAttributes(casProperties.getPersonDirectory().isReturnNull());
        bean.setPrincipalFactory(principalFactory());
        return bean;
    }

    @ConditionalOnMissingBean(name = "principalFactory")
    @Bean
    public PrincipalFactory principalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalFactory proxyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        final BasicPrincipalResolver p = new BasicPrincipalResolver();
        p.setPrincipalFactory(proxyPrincipalFactory());
        return p;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jaasAuthenticationHandler() {
        final JaasAuthenticationHandler h = new JaasAuthenticationHandler();

        h.setKerberosKdcSystemProperty(casProperties.getAuthn().getJaas().getKerberosKdcSystemProperty());
        h.setKerberosRealmSystemProperty(casProperties.getAuthn().getJaas().getKerberosRealmSystemProperty());
        h.setRealm(casProperties.getAuthn().getJaas().getRealm());
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getJaas().getPasswordEncoder()));

        if (passwordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(passwordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getJaas().getPrincipalTransformation()));

        h.setPrincipalFactory(jaasPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    @Autowired
    public AuthenticationHandler proxyAuthenticationHandler(@Qualifier(BEAN_NAME_HTTP_CLIENT) final HttpClient supportsTrustStoreSslSocketFactoryHttpClient) {
        final HttpBasedServiceCredentialsAuthenticationHandler h =
                new HttpBasedServiceCredentialsAuthenticationHandler();
        h.setHttpClient(supportsTrustStoreSslSocketFactoryHttpClient);
        h.setPrincipalFactory(proxyPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @ConditionalOnMissingBean(name = "authenticationHandlersResolvers")
    @Bean
    public Map authenticationHandlersResolvers(@Qualifier(BEAN_NAME_HTTP_CLIENT) final HttpClient httpClient) {
        final Map map = new HashMap<>();
        map.put(proxyAuthenticationHandler(httpClient), proxyPrincipalResolver());

        if (StringUtils.isNotBlank(casProperties.getAuthn().getJaas().getRealm())) {
            map.put(jaasAuthenticationHandler(),
                    personDirectoryPrincipalResolver());
        }

        return map;
    }

    @Bean
    public SimpleHttpClientFactoryBean.DefaultHttpClient httpClient() {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c =
                new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        return c;
    }

    @Bean
    public HttpClient noRedirectHttpClient() throws Exception {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c =
                new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        c.setRedirectsEnabled(false);
        c.setCircularRedirectsAllowed(false);
        c.setSslSocketFactory(trustStoreSslSocketFactory());
        return c.getObject();
    }

    @Bean
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() throws Exception {
        final SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        c.setConnectionTimeout(casProperties.getHttpClient().getConnectionTimeout());
        c.setReadTimeout(casProperties.getHttpClient().getReadTimeout());
        c.setSslSocketFactory(trustStoreSslSocketFactory());
        return c.getObject();
    }


    @Bean
    public AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy() {
        final DefaultAdaptiveAuthenticationPolicy p = new DefaultAdaptiveAuthenticationPolicy();
        p.setGeoLocationService(this.geoLocationService);
        p.setAdaptiveAuthenticationProperties(casProperties.getAuthn().getAdaptive());
        return p;
    }
}
