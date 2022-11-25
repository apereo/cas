package org.apereo.cas.web.security;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;
import org.apereo.cas.configuration.model.core.monitor.JaasSecurityActuatorEndpointsMonitorProperties;
import org.apereo.cas.configuration.model.core.monitor.LdapSecurityActuatorEndpointsMonitorProperties;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.CasWebSecurityConstants;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.security.authentication.EndpointLdapAuthenticationProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.jaas.JaasAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasWebSecurityConfigurerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Order(CasWebSecurityConstants.SECURITY_CONFIGURATION_ORDER)
@RequiredArgsConstructor
public class CasWebSecurityConfigurerAdapter implements DisposableBean {
    /**
     * Endpoint url used for admin-level form-login of endpoints.
     */
    public static final String ENDPOINT_URL_ADMIN_FORM_LOGIN = "/adminlogin";

    private final CasConfigurationProperties casProperties;

    private final SecurityProperties securityProperties;

    private final WebEndpointProperties webEndpointProperties;

    private final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints;

    private final List<ProtocolEndpointWebSecurityConfigurer> protocolEndpointWebSecurityConfigurers;

    private EndpointLdapAuthenticationProvider endpointLdapAuthenticationProvider;

    private static String prepareProtocolEndpoint(final String endpoint) {
        val baseEndpoint = StringUtils.prependIfMissing(endpoint, "/");
        val ext = FilenameUtils.getExtension(baseEndpoint);
        return StringUtils.isBlank(ext) ? baseEndpoint.concat("/**") : baseEndpoint.concat("**");
    }

    private static void configureJaasAuthenticationProvider(final HttpSecurity http,
                                                            final JaasSecurityActuatorEndpointsMonitorProperties jaas)
        throws Exception {
        val p = new JaasAuthenticationProvider();
        p.setLoginConfig(jaas.getLoginConfig());
        p.setLoginContextName(jaas.getLoginContextName());
        p.setRefreshConfigurationOnStartup(jaas.isRefreshConfigurationOnStartup());
        p.afterPropertiesSet();
        http.authenticationProvider(p);
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(endpointLdapAuthenticationProvider, EndpointLdapAuthenticationProvider::destroy);
    }

    /**
     * Disable Spring Security configuration for protocol endpoints
     * allowing CAS' own security configuration to handle protection
     * of endpoints where necessary.
     *
     * @param web web security
     */
    public void configureWebSecurity(final WebSecurity web) {
        val patterns = protocolEndpointWebSecurityConfigurers.stream()
            .map(ProtocolEndpointWebSecurityConfigurer::getIgnoredEndpoints)
            .flatMap(List<String>::stream)
            .map(CasWebSecurityConfigurerAdapter::prepareProtocolEndpoint)
            .collect(Collectors.toList());
        patterns.add("/webjars/**");
        patterns.add("/js/**");
        patterns.add("/css/**");
        patterns.add("/images/**");
        patterns.add("/static/**");
        patterns.add("/error");
        patterns.add("/favicon.ico");
        patterns.add("/");
        patterns.add(webEndpointProperties.getBasePath());
        LOGGER.debug("Configuring protocol endpoints [{}] to exclude/ignore from web security", patterns);
        web.debug(LOGGER.isDebugEnabled())
            .ignoring()
            .requestMatchers(patterns.toArray(String[]::new));
    }

    /**
     * Configure http security.
     *
     * @param http the http
     * @return the http security
     * @throws Exception the exception
     */
    public HttpSecurity configureHttpSecurity(final HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf()
            .disable()
            .headers()
            .disable()
            .logout()
            .disable()
            .requiresChannel()
            .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            .requiresSecure()
            .and();

        var requests = http.authorizeHttpRequests();
        val patterns = protocolEndpointWebSecurityConfigurers.stream()
            .map(ProtocolEndpointWebSecurityConfigurer::getIgnoredEndpoints)
            .flatMap(List<String>::stream)
            .map(CasWebSecurityConfigurerAdapter::prepareProtocolEndpoint)
            .collect(Collectors.toList());

        patterns.add("/webjars/**");
        patterns.add("/js/**");
        patterns.add("/css/**");
        patterns.add("/images/**");
        patterns.add("/static/**");
        patterns.add("/error");
        patterns.add("/favicon.ico");

        LOGGER.debug("Configuring protocol endpoints [{}] to exclude/ignore from http security", patterns);
        requests.requestMatchers(patterns.toArray(String[]::new))
            .permitAll()
            .and()
            .securityContext()
            .disable()
            .sessionManagement()
            .disable()
            .requestCache()
            .disable();

        protocolEndpointWebSecurityConfigurers.forEach(cfg -> cfg.configure(http));
        val endpoints = casProperties.getMonitor().getEndpoints().getEndpoint();
        endpoints.forEach(Unchecked.biConsumer((key, v) -> {
            val endpoint = EndpointRequest.to(key);
            v.getAccess().forEach(Unchecked.consumer(access -> configureEndpointAccess(http, requests, access, v, endpoint)));
        }));
        configureEndpointAccessToDenyUndefined(http, requests);
        configureEndpointAccessForStaticResources(requests);
        configureEndpointAccessByFormLogin(http);

        val jaas = casProperties.getMonitor().getEndpoints().getJaas();
        if (jaas.getLoginConfig() != null) {
            configureJaasAuthenticationProvider(http, jaas);
        } else {
            LOGGER.trace("No JAAS login config is defined to enable JAAS authentication");
        }

        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        if (StringUtils.isNotBlank(ldap.getLdapUrl()) && StringUtils.isNotBlank(ldap.getSearchFilter())) {
            configureLdapAuthenticationProvider(http, ldap);
        } else {
            LOGGER.trace("No LDAP url or search filter is defined to enable LDAP authentication");
        }
        return http;
    }

    /**
     * Configure endpoint access to deny undefined.
     *
     * @param http     the http
     * @param requests the requests
     */
    protected void configureEndpointAccessToDenyUndefined(
        final HttpSecurity http,
        final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        val endpoints = casProperties.getMonitor().getEndpoints().getEndpoint().keySet();
        val endpointDefaults = casProperties.getMonitor().getEndpoints().getDefaultEndpointProperties();
        pathMappedEndpoints.getObject().forEach(endpoint -> {
            val rootPath = endpoint.getRootPath();
            if (endpoints.contains(rootPath)) {
                LOGGER.trace("Endpoint security is defined for endpoint [{}]", rootPath);
            } else {
                val defaultAccessRules = endpointDefaults.getAccess();
                LOGGER.trace("Endpoint security is NOT defined for endpoint [{}]. Using default security rules [{}]", rootPath, endpointDefaults);
                val endpointRequest = EndpointRequest.to(rootPath).excludingLinks();
                defaultAccessRules.forEach(Unchecked.consumer(access ->
                    configureEndpointAccess(http, requests, access, endpointDefaults, endpointRequest)));
            }
        });
    }

    /**
     * Configure endpoint access for static resources.
     *
     * @param requests the requests
     */
    protected void configureEndpointAccessForStaticResources(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        requests
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            .permitAll();
        requests
            .requestMatchers("/resources/**")
            .permitAll()
            .requestMatchers("/static/**")
            .permitAll();
    }

    /**
     * Configure endpoint access by form login.
     *
     * @param http the http
     * @throws Exception the exception
     */
    protected void configureEndpointAccessByFormLogin(final HttpSecurity http) throws Exception {
        if (casProperties.getMonitor().getEndpoints().isFormLoginEnabled()) {
            http.formLogin().loginPage(ENDPOINT_URL_ADMIN_FORM_LOGIN).permitAll();
        } else {
            http.formLogin().disable();
        }
    }

    /**
     * Configure endpoint access.
     *
     * @param httpSecurity the httpSecurity
     * @param requests     the requests
     * @param access       the access
     * @param properties   the properties
     * @param endpoint     the endpoint
     * @throws Exception the exception
     */
    protected void configureEndpointAccess(final HttpSecurity httpSecurity,
                                           final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
                                           final ActuatorEndpointProperties.EndpointAccessLevel access,
                                           final ActuatorEndpointProperties properties,
                                           final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        switch (access) {
            case AUTHORITY -> configureEndpointAccessByAuthority(httpSecurity, requests, properties, endpoint);
            case ROLE -> configureEndpointAccessByRole(httpSecurity, requests, properties, endpoint);
            case AUTHENTICATED -> configureEndpointAccessAuthenticated(httpSecurity, requests, endpoint);
            case IP_ADDRESS -> configureEndpointAccessByIpAddress(requests, properties, endpoint);
            case PERMIT -> configureEndpointAccessPermitAll(requests, endpoint);
            case ANONYMOUS -> configureEndpointAccessAnonymously(requests, endpoint);
            default -> configureEndpointAccessToDenyAll(requests, endpoint);
        }
    }

    protected void configureEndpointAccessPermitAll(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
                                                    final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests.requestMatchers(endpoint).permitAll();
    }

    protected void configureEndpointAccessToDenyAll(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
                                                    final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests.requestMatchers(endpoint).denyAll();
    }

    protected void configureEndpointAccessAnonymously(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
                                                      final EndpointRequest.EndpointRequestMatcher endpoint) {

        requests.requestMatchers(endpoint).permitAll();
    }

    protected void configureEndpointAccessByIpAddress(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
                                                      final ActuatorEndpointProperties properties,
                                                      final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests
            .requestMatchers(endpoint)
            .access((authentication, context) -> {
                val remoteAddr = StringUtils.defaultIfBlank(
                    context.getRequest().getHeader(casProperties.getAudit().getEngine().getAlternateClientAddrHeaderName()),
                    context.getRequest().getRemoteAddr());

                val addresses = properties.getRequiredIpAddresses()
                    .stream()
                    .filter(addr -> FunctionUtils.doAndHandle(() -> {
                        val ipAddressMatcher = new IpAddressMatcher(addr);
                        LOGGER.trace("Attempting to match [{}] against [{}] as a IP or netmask", remoteAddr, addr);
                        return ipAddressMatcher.matches(remoteAddr);
                    }, e -> {
                        val matcher = RegexUtils.createPattern(addr, Pattern.CASE_INSENSITIVE).matcher(remoteAddr);
                        LOGGER.trace("Attempting to match [{}] against [{}] as a regular expression", remoteAddr, addr);
                        return matcher.matches();
                    }).get())
                    .findFirst();
                val granted = addresses.isPresent();
                if (!granted) {
                    LOGGER.warn("Provided regular expression or IP/netmask [{}] does not match [{}]", properties.getRequiredIpAddresses(), remoteAddr);
                }
                return new AuthorizationDecision(granted);
            });
    }

    protected void configureEndpointAccessAuthenticated(
        final HttpSecurity http,
        final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
        final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .authenticated()
            .and()
            .httpBasic();
    }

    protected void configureEndpointAccessByRole(
        final HttpSecurity http,
        final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
        final ActuatorEndpointProperties properties,
        final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .hasAnyRole(properties.getRequiredRoles().toArray(ArrayUtils.EMPTY_STRING_ARRAY))
            .and()
            .httpBasic();
    }

    protected void configureEndpointAccessByAuthority(
        final HttpSecurity http,
        final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests,
        final ActuatorEndpointProperties properties,
        final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .hasAnyAuthority(properties.getRequiredAuthorities().toArray(ArrayUtils.EMPTY_STRING_ARRAY))
            .and()
            .httpBasic();
    }

    private boolean isLdapAuthorizationActive() {
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        return StringUtils.isNotBlank(ldap.getBaseDn())
               && StringUtils.isNotBlank(ldap.getLdapUrl())
               && StringUtils.isNotBlank(ldap.getSearchFilter())
               && (StringUtils.isNotBlank(ldap.getLdapAuthz().getRoleAttribute())
                   || StringUtils.isNotBlank(ldap.getLdapAuthz().getGroupAttribute()));
    }

    private void configureLdapAuthenticationProvider(final HttpSecurity http,
                                                     final LdapSecurityActuatorEndpointsMonitorProperties ldap) {
        if (isLdapAuthorizationActive()) {
            val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
            val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
            this.endpointLdapAuthenticationProvider = new EndpointLdapAuthenticationProvider(ldap,
                securityProperties, connectionFactory, authenticator);
            http.authenticationProvider(endpointLdapAuthenticationProvider);
        }
    }
}
