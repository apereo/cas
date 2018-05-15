package org.apereo.cas.web.security;

import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningStandaloneCondition;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link BaseCasLdapUserDetailsManagerConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
@Category(LdapCategory.class)
public abstract class BaseCasLdapUserDetailsManagerConfigurerTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyUserDetails() throws Exception {
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
