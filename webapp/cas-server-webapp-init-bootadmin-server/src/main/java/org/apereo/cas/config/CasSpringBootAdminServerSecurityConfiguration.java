package org.apereo.cas.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * This is {@link CasSpringBootAdminServerSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "casSpringBootAdminServerSecurityConfiguration", proxyBeanMethods = false)
public class CasSpringBootAdminServerSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private AdminServerProperties adminServerProperties;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        val adminContextPath = adminServerProperties.getContextPath();

        val successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + '/');
        http.authorizeRequests()
            .antMatchers(adminContextPath + "/assets/**").permitAll()
            .antMatchers(adminContextPath + "/login").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and()
            .logout().logoutUrl(adminContextPath + "/logout").and()
            .httpBasic().and()
            .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringAntMatchers(
                adminContextPath + "/instances",
                adminContextPath + "/actuator/**"
            );
    }
}
