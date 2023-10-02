package org.apereo.cas.config;

import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasAuthenticationDelegationTestConfiguration}.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@TestConfiguration(value = "CasAuthenticationDelegationTestConfiguration", proxyBeanMethods = false)
public class CasAuthenticationDelegationTestConfiguration {

    @Bean
    public SessionStore delegatedClientDistributedSessionStore() {
        return new JEESessionStore();
    }
}
