package org.apereo.cas.web.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@AllArgsConstructor
public class CasJdbcUserDetailsManagerConfigurer extends JdbcUserDetailsManagerConfigurer<AuthenticationManagerBuilder> {
    private final AdminPagesSecurityProperties adminPagesSecurityProperties;

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        usersByUsernameQuery(adminPagesSecurityProperties.getJdbc().getQuery());
        rolePrefix(adminPagesSecurityProperties.getJdbc().getRolePrefix());
        dataSource(JpaBeans.newDataSource(adminPagesSecurityProperties.getJdbc()));
        passwordEncoder(PasswordEncoderUtils.newPasswordEncoder(adminPagesSecurityProperties.getJdbc().getPasswordEncoder()));
        super.configure(auth);
    }
}
