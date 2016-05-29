package org.apereo.cas.web.config;

import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.LdapAcceptableUsagePolicyRepository;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyLdapConfiguration")
public class CasSupportActionsAcceptableUsagePolicyLdapConfiguration {
    
    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository ldapAcceptableUsagePolicyRepository() {
        return new LdapAcceptableUsagePolicyRepository();
    }
}
