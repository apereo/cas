package org.apereo.cas.web.security;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.security.authentication.LdapAuthenticationProvider;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.authentication.jaas.JaasAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import java.util.stream.Collectors;

/**
 * This is {@link CasWebSecurityConfigurerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CasWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private final CasConfigurationProperties casProperties;
    private final SecurityProperties securityProperties;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .logout()
            .disable()
            .requiresChannel()
            .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
            .requiresSecure();

        val requests = http.authorizeRequests();
        configureEndpointAccessToDenyUndefined(requests);
        configureEndpointAccessForStaticResources(requests);

        val endpoints = casProperties.getMonitor().getEndpoints().getEndpoint();
        endpoints.forEach(Unchecked.biConsumer((k, v) -> {
            val endpoint = EndpointRequest.to(k);
            v.getAccess().forEach(Unchecked.consumer(access -> configureEndpointAccess(http, requests, access, v, endpoint)));
        }));
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        val jaas = casProperties.getMonitor().getEndpoints().getJaas();
        if (jaas.getLoginConfig() != null) {
            configureJaasAuthenticationProvider(auth, jaas);
        }

        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        if (StringUtils.isNotBlank(ldap.getLdapUrl()) && StringUtils.isNotBlank(ldap.getSearchFilter())) {
            configureLdapAuthenticationProvider(auth, ldap);
        }

        val jdbc = casProperties.getMonitor().getEndpoints().getJdbc();
        if (StringUtils.isNotBlank(jdbc.getQuery())) {
            configureJdbcAuthenticationProvider(auth, jdbc);
        }

        if (!auth.isConfigured()) {
            super.configure(auth);
        }
    }

    /**
     * Configure jdbc authentication provider.
     *
     * @param auth the auth
     * @param jdbc the jdbc
     * @throws Exception the exception
     */
    protected void configureJdbcAuthenticationProvider(final AuthenticationManagerBuilder auth, final MonitorProperties.Endpoints.JdbcSecurity jdbc) throws Exception {
        val cfg = auth.jdbcAuthentication();
        cfg.usersByUsernameQuery(jdbc.getQuery());
        cfg.rolePrefix(jdbc.getRolePrefix());
        cfg.dataSource(JpaBeans.newDataSource(jdbc));
        cfg.passwordEncoder(PasswordEncoderUtils.newPasswordEncoder(jdbc.getPasswordEncoder()));
    }

    /**
     * Configure ldap authentication provider.
     *
     * @param auth the auth
     * @param ldap the ldap
     */
    protected void configureLdapAuthenticationProvider(final AuthenticationManagerBuilder auth, final MonitorProperties.Endpoints.LdapSecurity ldap) {
        if (isLdapAuthorizationActive()) {
            val p = new LdapAuthenticationProvider(ldap, securityProperties);
            auth.authenticationProvider(p);
        }
    }

    /**
     * Configure jaas authentication provider.
     *
     * @param auth the auth
     * @param jaas the jaas
     * @throws Exception the exception
     */
    protected void configureJaasAuthenticationProvider(final AuthenticationManagerBuilder auth,
                                                       final MonitorProperties.Endpoints.JaasSecurity jaas) throws Exception {
        val p = new JaasAuthenticationProvider();
        p.setLoginConfig(jaas.getLoginConfig());
        p.setLoginContextName(jaas.getLoginContextName());
        p.setRefreshConfigurationOnStartup(jaas.isRefreshConfigurationOnStartup());
        p.afterPropertiesSet();
        auth.authenticationProvider(p);
    }


    /**
     * Configure endpoint access to deny undefined.
     *
     * @param requests the requests
     */
    protected void configureEndpointAccessToDenyUndefined(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests) {
        val endpoints = casProperties.getMonitor().getEndpoints().getEndpoint().keySet();
        val configuredEndpoints = endpoints.toArray(new String[]{});
        requests
            .requestMatchers(EndpointRequest.toAnyEndpoint().excluding(configuredEndpoints).excludingLinks())
            .denyAll();
    }

    /**
     * Configure endpoint access for static resources.
     *
     * @param requests the requests
     */
    protected void configureEndpointAccessForStaticResources(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests) {
        requests
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            .permitAll();
        requests
            .antMatchers("/resources/**")
            .permitAll()
            .antMatchers("/static/**")
            .permitAll();
    }

    /**
     * Configure endpoint access by form login.
     *
     * @param requests the requests
     * @throws Exception the exception
     */
    protected void configureEndpointAccessByFormLogin(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests) throws Exception {
        requests.and()
            .formLogin()
            .loginPage("/adminlogin")
            .permitAll();
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
                                           final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                           final ActuatorEndpointProperties.EndpointAccessLevel access,
                                           final ActuatorEndpointProperties properties,
                                           final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        switch (access) {
            case AUTHORITY:
                configureEndpointAccessByAuthority(requests, properties, endpoint);
                configureEndpointAccessByFormLogin(requests);
                break;
            case ROLE:
                configureEndpointAccessByRole(requests, properties, endpoint);
                configureEndpointAccessByFormLogin(requests);
                break;
            case AUTHENTICATED:
                configureEndpointAccessAuthenticated(requests, endpoint);
                configureEndpointAccessByFormLogin(requests);
                break;
            case IP_ADDRESS:
                configureEndpointAccessByIpAddress(requests, properties, endpoint);
                break;
            case PERMIT:
                configureEndpointAccessPermitAll(requests, endpoint);
                break;
            case ANONYMOUS:
                configureEndpointAccessAnonymously(requests, endpoint);
                break;
            case DENY:
            default:
                configureEndpointAccessToDenyAll(requests, endpoint);
                break;
        }
    }

    private void configureEndpointAccessPermitAll(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                  final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests.requestMatchers(endpoint).permitAll();
    }

    private void configureEndpointAccessToDenyAll(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                  final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests.requestMatchers(endpoint).denyAll();
    }

    private void configureEndpointAccessAnonymously(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                    final EndpointRequest.EndpointRequestMatcher endpoint) {
        requests.requestMatchers(endpoint).anonymous();
    }

    private void configureEndpointAccessByIpAddress(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                    final ActuatorEndpointProperties properties,
                                                    final EndpointRequest.EndpointRequestMatcher endpoint) {
        val addresses = properties.getRequiredIpAddresses()
            .stream()
            .map(address -> "hasIpAddress('" + address + "')")
            .collect(Collectors.joining(" or "));
        requests
            .requestMatchers(endpoint)
            .access(addresses);
    }

    private void configureEndpointAccessAuthenticated(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                      final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .authenticated()
            .and()
            .httpBasic();
    }

    private void configureEndpointAccessByRole(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                               final ActuatorEndpointProperties properties,
                                               final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .hasAnyRole(properties.getRequiredRoles().toArray(new String[]{}))
            .and()
            .httpBasic();
    }

    private void configureEndpointAccessByAuthority(final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                                    final ActuatorEndpointProperties properties,
                                                    final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        requests.requestMatchers(endpoint)
            .hasAnyAuthority(properties.getRequiredAuthorities().toArray(new String[]{}))
            .and()
            .httpBasic();
    }

    private boolean isLdapAuthorizationActive() {
        val ldap = casProperties.getMonitor().getEndpoints().getLdap();
        val authZ = ldap.getLdapAuthz();
        return StringUtils.isNotBlank(ldap.getBaseDn())
            && StringUtils.isNotBlank(ldap.getLdapUrl())
            && StringUtils.isNotBlank(ldap.getSearchFilter())
            && (StringUtils.isNotBlank(authZ.getRoleAttribute()) || StringUtils.isNotBlank(authZ.getGroupAttribute()));
    }
}
