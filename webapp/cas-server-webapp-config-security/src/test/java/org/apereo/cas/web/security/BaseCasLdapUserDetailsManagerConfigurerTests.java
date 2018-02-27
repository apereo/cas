package org.apereo.cas.web.security;

import lombok.SneakyThrows;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.apereo.cas.util.junit.RunningStandaloneCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import static org.junit.Assert.*;

/**
 * This is {@link BaseCasLdapUserDetailsManagerConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@RunWith(ConditionalSpringRunner.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public abstract class BaseCasLdapUserDetailsManagerConfigurerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @SneakyThrows
    @Test
    public void verifyUserDetails() {
        final CasLdapUserDetailsManagerConfigurer cfg = new CasLdapUserDetailsManagerConfigurer(casProperties.getAdminPagesSecurity());
        final AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(new AuthenticationObjectPostProcessor());
        cfg.configure(builder);
        final AuthenticationManager mgr = builder.build();
        assertNotNull(mgr);
    }

    private static class AuthenticationObjectPostProcessor implements ObjectPostProcessor {
        @Override
        public Object postProcess(final Object o) {
            return o;
        }
    }
}
