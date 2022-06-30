package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * This is {@link CasSpringBootAdminServerSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SpringBootAdmin)
@AutoConfiguration
public class CasSpringBootAdminServerSecurityConfiguration {
    private final AdminServerProperties adminServerProperties;

    @Bean
    @ConditionalOnMissingBean(name = "springBootAdminSecurityAdapter")
    public SecurityFilterChain springBootAdminSecurityAdapter(final HttpSecurity http) throws Exception {
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
        return http.build();
    }
}
