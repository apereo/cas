package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthCodeExpirationPolicy;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.device.DeviceTokenImpl;
import org.apereo.cas.ticket.device.DeviceUserCodeImpl;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurator;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuthComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casOAuthComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthComponentSerializationConfiguration implements ComponentSerializationPlanConfigurator {

    @Override
    public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
        plan.registerSerializableClass(OAuthAccessTokenExpirationPolicy.class);
        plan.registerSerializableClass(OAuthRefreshTokenExpirationPolicy.class);
        plan.registerSerializableClass(OAuthRefreshTokenExpirationPolicy.OAuthRefreshTokenStandaloneExpirationPolicy.class);
        plan.registerSerializableClass(OAuthCodeExpirationPolicy.class);

        plan.registerSerializableClass(OAuthRegisteredService.class);

        plan.registerSerializableClass(DefaultRegisteredServiceOAuthCodeExpirationPolicy.class);
        plan.registerSerializableClass(DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy.class);
        plan.registerSerializableClass(DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy.class);

        plan.registerSerializableClass(OAuthCodeImpl.class);
        plan.registerSerializableClass(AccessTokenImpl.class);
        plan.registerSerializableClass(RefreshTokenImpl.class);
        plan.registerSerializableClass(DeviceTokenImpl.class);
        plan.registerSerializableClass(DeviceUserCodeImpl.class);
    }
}
