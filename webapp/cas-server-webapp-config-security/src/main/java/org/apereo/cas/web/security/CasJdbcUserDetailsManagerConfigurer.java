package org.apereo.cas.web.security;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;

/**
 * This is {@link CasJdbcUserDetailsManagerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasJdbcUserDetailsManagerConfigurer extends JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> {
    private final AdminPagesSecurityProperties adminPagesSecurityProperties;

    public CasJdbcUserDetailsManagerConfigurer(final AdminPagesSecurityProperties securityProperties) {
        this.adminPagesSecurityProperties = securityProperties;
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        usersByUsernameQuery(adminPagesSecurityProperties.getJdbc().getQuery());
        rolePrefix(adminPagesSecurityProperties.getJdbc().getRolePrefix());
        dataSource(JpaBeans.newDataSource(adminPagesSecurityProperties.getJdbc()));
        passwordEncoder(PasswordEncoderUtils.newPasswordEncoder(adminPagesSecurityProperties.getJdbc().getPasswordEncoder()));
        super.configure(auth);
    }
}
