package org.apereo.cas.web.security;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.JdbcSecurityActuatorEndpointsMonitorProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.CasWebSecurityConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * This is {@link CasWebSecurityJdbcConfigurerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Order(CasWebSecurityConstants.SECURITY_CONFIGURATION_ORDER - 1)
public class CasWebSecurityJdbcConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        val jdbc = casProperties.getMonitor().getEndpoints().getJdbc();
        if (StringUtils.isNotBlank(jdbc.getQuery())) {
            configureJdbcAuthenticationProvider(auth, jdbc);
        } else {
            LOGGER.trace("No JDBC query is defined to enable JDBC authentication");
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
    protected void configureJdbcAuthenticationProvider(final AuthenticationManagerBuilder auth,
                                                       final JdbcSecurityActuatorEndpointsMonitorProperties jdbc)
        throws Exception {
        val passwordEncoder = PasswordEncoderUtils.newPasswordEncoder(jdbc.getPasswordEncoder(), applicationContext);
        auth.jdbcAuthentication()
            .passwordEncoder(passwordEncoder)
            .usersByUsernameQuery(jdbc.getQuery())
            .rolePrefix(jdbc.getRolePrefix())
            .dataSource(JpaBeans.newDataSource(jdbc));
    }
}
