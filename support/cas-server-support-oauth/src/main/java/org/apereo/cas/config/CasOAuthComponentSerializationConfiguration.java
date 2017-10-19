package org.apereo.cas.config;

import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.ComponentSerializationPlanConfigurator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casOAuthComponentSerializationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {
    
    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(OAuthAccessTokenExpirationPolicy.class);
        plan.registerSerializableClass(OAuthRefreshTokenExpirationPolicy.class);
        plan.registerSerializableClass(OAuthCodeExpirationPolicy.class);

        plan.registerSerializableClass(OAuthCodeImpl.class);
        plan.registerSerializableClass(AccessTokenImpl.class);
        plan.registerSerializableClass(RefreshTokenImpl.class);
    }
}
