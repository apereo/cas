package org.apereo.cas.web.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.ActuatorEndpointProperties;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
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
@Slf4j
@RequiredArgsConstructor
public class CasWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private final CasConfigurationProperties casProperties;


    @Override
    @SneakyThrows
    protected void configure(final AuthenticationManagerBuilder auth) {
        super.configure(auth);
        final var jaas = casProperties.getMonitor().getEndpoints().getJaas();
        if (jaas.getLoginConfig() != null) {
            final var p = new JaasAuthenticationProvider();
            p.setLoginConfig(jaas.getLoginConfig());
            p.setLoginContextName(jaas.getLoginContextName());
            p.setRefreshConfigurationOnStartup(jaas.isRefreshConfigurationOnStartup());
            p.afterPropertiesSet();
            auth.authenticationProvider(p);
        }
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final var requests = http.authorizeRequests();
        requests.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();

        final var endpoints = casProperties.getMonitor().getEndpoints().getEndpoint();

        final var configuredEndpoints = endpoints.keySet().toArray(new String[]{});
        requests.requestMatchers(EndpointRequest.toAnyEndpoint().excluding(configuredEndpoints)).denyAll();
        requests
            .antMatchers("/resources/**")
            .permitAll()
            .antMatchers("/static/**")
            .permitAll();

        endpoints.forEach(Unchecked.biConsumer((k, v) -> {
            final var endpoint = EndpointRequest.to(k);
            v.getAccess().forEach(Unchecked.consumer(access -> configureEndpointAccessLevel(http, requests, access, v, endpoint)));
        }));
    }

    private void configureEndpointAccessLevel(final HttpSecurity http,
                                              final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests,
                                              final ActuatorEndpointProperties.EndpointAccessLevel access,
                                              final ActuatorEndpointProperties properties,
                                              final EndpointRequest.EndpointRequestMatcher endpoint) throws Exception {
        switch (access) {
            case AUTHORITY:
                requests.requestMatchers(endpoint)
                    .hasAnyAuthority(properties.getRequiredAuthorities().toArray(new String[]{}))
                    .and()
                    .httpBasic();
                break;
            case ROLE:
                requests.requestMatchers(endpoint)
                    .hasAnyRole(properties.getRequiredRoles().toArray(new String[]{}))
                    .and()
                    .httpBasic();
                break;
            case AUTHENTICATED:
                requests.requestMatchers(endpoint)
                    .authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .formLogin()
                    .loginPage("/adminlogin")
                    .permitAll();
                break;
            case IP_ADDRESS:
                final var addresses = properties.getRequiredIpAddresses()
                    .stream()
                    .map(address -> "hasIpAddress('" + address + "')")
                    .collect(Collectors.joining(" or "));
                requests.requestMatchers(endpoint).access(addresses);
                break;
            case PERMIT:
                requests.requestMatchers(endpoint).permitAll();
                break;
            case ANONYMOUS:
                requests.requestMatchers(endpoint).anonymous();
                break;
            case DENY:
            default:
                requests.requestMatchers(endpoint).denyAll();
                break;
        }
    }
}
