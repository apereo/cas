package org.apereo.cas.web.config;

import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.web.flow.AcceptableUsagePolicyRepository;
import org.apereo.cas.web.flow.LdapAcceptableUsagePolicyRepository;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * This is {@link CasSupportActionsAcceptableUsagePolicyLdapConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsAcceptableUsagePolicyLdapConfiguration")
public class CasSupportActionsAcceptableUsagePolicyLdapConfiguration {

    @Resource(name = "ldapUsagePolicyConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    private AcceptableUsagePolicyProperties properties;

    @RefreshScope
    @Bean
    public AcceptableUsagePolicyRepository ldapAcceptableUsagePolicyRepository() {
        final LdapAcceptableUsagePolicyRepository r =
                new LdapAcceptableUsagePolicyRepository();
        r.setBaseDn(properties.getLdap().getBaseDn());
        r.setConnectionFactory(this.connectionFactory);
        r.setSearchFilter(properties.getLdap().getSearchFilter());
        r.setAupAttributeName(properties.getAupAttributeName());
        return r;
    }
}
