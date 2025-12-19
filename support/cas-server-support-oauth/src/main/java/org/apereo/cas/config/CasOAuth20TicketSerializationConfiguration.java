package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceToken;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasOAuth20TicketSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@Configuration(value = "CasOAuth20TicketSerializationConfiguration", proxyBeanMethods = false)
class CasOAuth20TicketSerializationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketSerializationExecutionPlanConfigurer oauthTicketSerializationExecutionPlanConfigurer(final ConfigurableApplicationContext applicationContext) {
        return plan -> {
            plan.registerTicketSerializer(new OAuthCodeTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(new AccessTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(new RefreshTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(new DeviceTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(new DeviceUserCodeTicketStringSerializer(applicationContext));

            plan.registerTicketSerializer(OAuth20Code.class.getName(), new OAuthCodeTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20AccessToken.class.getName(), new AccessTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20RefreshToken.class.getName(), new RefreshTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20DeviceToken.class.getName(), new DeviceTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20DeviceUserCode.class.getName(), new DeviceUserCodeTicketStringSerializer(applicationContext));

            plan.registerTicketSerializer(OAuth20Code.PREFIX, new OAuthCodeTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20AccessToken.PREFIX, new AccessTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20RefreshToken.PREFIX, new RefreshTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20DeviceToken.PREFIX, new DeviceTokenTicketStringSerializer(applicationContext));
            plan.registerTicketSerializer(OAuth20DeviceUserCode.PREFIX, new DeviceUserCodeTicketStringSerializer(applicationContext));
        };
    }

    private static final class OAuthCodeTicketStringSerializer extends BaseJacksonSerializer<OAuth20DefaultCode> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        OAuthCodeTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OAuth20DefaultCode.class);
        }
    }

    private static final class AccessTokenTicketStringSerializer extends BaseJacksonSerializer<OAuth20DefaultAccessToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        AccessTokenTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OAuth20DefaultAccessToken.class);
        }
    }

    private static final class RefreshTokenTicketStringSerializer extends BaseJacksonSerializer<OAuth20DefaultRefreshToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        RefreshTokenTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OAuth20DefaultRefreshToken.class);
        }
    }

    private static final class DeviceTokenTicketStringSerializer extends BaseJacksonSerializer<OAuth20DefaultDeviceToken> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        DeviceTokenTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OAuth20DefaultDeviceToken.class);
        }
    }

    private static final class DeviceUserCodeTicketStringSerializer extends BaseJacksonSerializer<OAuth20DefaultDeviceUserCode> {
        @Serial
        private static final long serialVersionUID = -2198623586274810263L;

        DeviceUserCodeTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
            super(MINIMAL_PRETTY_PRINTER, applicationContext, OAuth20DefaultDeviceUserCode.class);
        }
    }
}
